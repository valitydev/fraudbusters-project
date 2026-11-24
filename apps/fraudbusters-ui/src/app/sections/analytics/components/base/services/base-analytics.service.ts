import { Injectable } from '@angular/core';
import { Observable, ReplaySubject, forkJoin, of } from 'rxjs';
import { catchError, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

import { FraudResultSummary } from '../../../../../api/fb-management/swagger-codegen/model/fraudResultSummary';
import { AnalyticsService } from '../../../../../api/payments/analytics';
import { SearchBaseAnalyticsParams } from '../../../../../api/payments/analytics/searchBaseAnalyticsParams';
import { ChartData } from '../../../model/chart-data';

interface AnalyticsData {
    currency: string;
    attempted: number;
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
                        const topDeclines = summaries
                            .filter(
                                (row) =>
                                    row.status?.toUpperCase() === 'DECLINE' &&
                                    !!row.checkedRule &&
                                    row.summary.count > 0
                            )
                            .slice(0, 5);
                        const ratio = Number.isFinite(result.ratio) ? result.ratio : null;
                        const chart = this.withDailySlots(result.chart, params);
                        return { data: { ...result, chart, ratio, currency: params.currency, summaries, topDeclines } };
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

    private withDailySlots(chart: ChartData, params: SearchBaseAnalyticsParams): ChartData {
        if (params.splitUnit !== 'day' || !chart.series.length) {
            return chart;
        }
        const firstDay = Date.parse(params.fromTime.slice(0, 10) + 'T00:00:00Z');
        const lastDay = Date.parse(params.toTime.slice(0, 10) + 'T00:00:00Z');
        const dayMs = 24 * 60 * 60 * 1000;
        const dates = Array.from({ length: (lastDay - firstDay) / dayMs + 1 }, (_, index) => firstDay + index * dayMs);
        const formatter = new Intl.DateTimeFormat('en-GB', { day: 'numeric', month: 'short', timeZone: 'UTC' });
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
