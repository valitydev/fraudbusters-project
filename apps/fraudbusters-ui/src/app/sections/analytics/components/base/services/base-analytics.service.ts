import { Injectable } from '@angular/core';
import { Observable, ReplaySubject, forkJoin, of } from 'rxjs';
import { catchError, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

import { FraudResultSummary } from '../../../../../api/fb-management/swagger-codegen/model/fraudResultSummary';
import { AnalyticsService } from '../../../../../api/payments/analytics';
import { SearchBaseAnalyticsParams } from '../../../../../api/payments/analytics/searchBaseAnalyticsParams';
import { ChartData } from '../../../model/chart-data';

export interface AnalyticsData {
    parameters: SearchBaseAnalyticsParams;
    declineCount: number;
    currency: string;
    attempted: number;
    attemptedChange: number | null;
    blocked: number;
    ratio: number | null;
    blockedAmount: number;
    chart: ChartData;
    summaries: FraudResultSummary[];
    topDeclines: FraudResultSummary[];
}

export interface AnalyticsState {
    loading?: boolean;
    error?: boolean;
    data?: AnalyticsData;
}

@Injectable()
export class BaseAnalyticsService {
    readonly currencies$ = this.analyticsService.getCurrencies().pipe(shareReplay({ bufferSize: 1, refCount: true }));
    readonly state$: Observable<AnalyticsState>;

    private readonly parameters$ = new ReplaySubject<SearchBaseAnalyticsParams>(1);

    private lastParameters?: SearchBaseAnalyticsParams;

    constructor(private analyticsService: AnalyticsService) {
        this.state$ = this.parameters$.pipe(
            switchMap((params) =>
                forkJoin({
                    attempted: this.analyticsService.getAttemptedPaymentsCount(params),
                    previousAttempted: this.analyticsService
                        .getAttemptedPaymentsCount(this.previousPeriod(params))
                        .pipe(catchError(() => of(null))),
                    blocked: this.analyticsService.getBlockedPaymentsCount(params),
                    ratio: this.analyticsService.getBlockedPaymentsRatio(params),
                    blockedAmount: this.analyticsService.getBlockedPaymentsSum(params),
                    chart: this.analyticsService.getSplitCountRatio(params),
                    summaries: this.analyticsService.getSummary(params),
                }).pipe(
                    map((result): AnalyticsState => {
                        const summaries = [...(result.summaries ?? [])].sort(
                            (a, b) => b.summary.count - a.summary.count
                        );
                        const declines = summaries.filter(
                            (row) => row.status?.toUpperCase() === 'DECLINE' && row.summary.count > 0
                        );
                        const topDeclines = declines.filter((row) => !!row.checkedRule).slice(0, 5);
                        const declineCount = declines.reduce((total, row) => total + row.summary.count, 0);
                        const ratio = Number.isFinite(result.ratio) && result.ratio >= 0 ? result.ratio : null;
                        const chart = this.withTimeSlots(result.chart, params);
                        const attemptedChange =
                            result.previousAttempted > 0
                                ? (result.attempted - result.previousAttempted) / result.previousAttempted
                                : null;
                        return {
                            data: {
                                ...result,
                                chart,
                                attemptedChange,
                                ratio,
                                currency: params.currency,
                                parameters: params,
                                summaries,
                                declineCount,
                                topDeclines,
                            },
                        };
                    }),
                    startWith({ loading: true } as const),
                    catchError(() => of({ error: true } as const))
                )
            ),
            shareReplay({ bufferSize: 1, refCount: true })
        );
    }

    search(params: SearchBaseAnalyticsParams): void {
        this.lastParameters = params;
        this.parameters$.next(params);
    }

    retry(): void {
        if (this.lastParameters) {
            this.parameters$.next(this.lastParameters);
        }
    }

    private previousPeriod(params: SearchBaseAnalyticsParams): SearchBaseAnalyticsParams {
        const from = Date.parse(params.fromTime.replace(' ', 'T') + 'Z');
        const to = Date.parse(params.toTime.replace(' ', 'T') + 'Z');
        const day = 24 * 60 * 60 * 1000;
        const shift = (Math.floor(to / day) - Math.floor(from / day) + 1) * day;
        const format = (time: number) => new Date(time - shift).toISOString().slice(0, 19).replace('T', ' ');
        return { ...params, fromTime: format(from), toTime: format(to) };
    }

    private withTimeSlots(chart: ChartData, params: SearchBaseAnalyticsParams): ChartData {
        const hourly = params.splitUnit === 'hour';
        if (!chart.series.some((series) => series.data.some((point) => point.y != null))) {
            return { ...chart, series: [] };
        }
        if ((!hourly && params.splitUnit !== 'day') || !chart.series.length) {
            return chart;
        }
        const step = (hourly ? 1 : 24) * 60 * 60 * 1000;
        const slotStart = (time: string) => Math.floor(Date.parse(time.replace(' ', 'T') + 'Z') / step) * step;
        const first = slotStart(params.fromTime);
        const last = slotStart(params.toTime);
        const dates = Array.from({ length: (last - first) / step + 1 }, (_, index) => first + index * step);
        const crossesMidnight = params.fromTime.slice(0, 10) !== params.toTime.slice(0, 10);
        const formatter = new Intl.DateTimeFormat('en-GB', {
            ...(!hourly || crossesMidnight ? ({ day: 'numeric', month: 'short' } as const) : {}),
            ...(hourly ? ({ hour: '2-digit', minute: '2-digit' } as const) : {}),
            timeZone: 'UTC',
        });
        return {
            ...chart,
            categorical: true,
            series: chart.series.map((series) => {
                const byDate = new Map(series.data.map((point) => [new Date(point.x).getTime(), point]));
                return {
                    ...series,
                    data: dates.map((date) => ({ y: null, ...byDate.get(date), x: formatter.format(date) })),
                };
            }),
        };
    }
}
