import { Injectable } from '@angular/core';
import { EMPTY, merge, Observable, Subject } from 'rxjs';
import { catchError, filter, shareReplay, switchMap } from 'rxjs/operators';

import { FraudResultSummary } from '../../../../../api/fb-management/swagger-codegen/model/fraudResultSummary';
import { AnalyticsService } from '../../../../../api/payments/analytics';
import { SearchBaseAnalyticsParams } from '../../../../../api/payments/analytics/searchBaseAnalyticsParams';
import { progress } from '../../../../../shared/operators';
import { ChartData } from '../../../model/chart-data';

@Injectable()
export class BaseAnalyticsService {
    currencies$: Observable<string[]>;
    attemptedPayments$: Observable<number>;
    blockedPayments$: Observable<number>;
    ratioOfBlocked$: Observable<number>;
    blockedSum$: Observable<number>;
    fraudSummary$: Observable<FraudResultSummary[]>;

    chartDataX$: Observable<ChartData>;

    searchParameters$ = new Subject<SearchBaseAnalyticsParams>();

    finished$ = new Subject<unknown>();
    inProgress$ = progress(this.searchParameters$, this.finished$, true).pipe(shareReplay(1));

    constructor(private analyticsService: AnalyticsService) {
        this.currencies$ = this.analyticsService.getCurrencies();
        this.searchParameters$.subscribe();

        this.fraudSummary$ = this.searchParameters$.pipe(
            switchMap((value) => this.analyticsService.getSummary(value)),
            filter((r) => !!r),
            shareReplay(1)
        );
        this.attemptedPayments$ = this.searchParameters$.pipe(
            switchMap((value) => this.analyticsService.getAttemptedPaymentsCount(value)),
            shareReplay(1)
        );
        this.blockedPayments$ = this.searchParameters$.pipe(
            switchMap((value) => this.analyticsService.getBlockedPaymentsCount(value)),
            shareReplay(1)
        );
        this.ratioOfBlocked$ = this.searchParameters$.pipe(
            switchMap((value) => this.analyticsService.getBlockedPaymentsRatio(value)),
            shareReplay(1)
        );
        this.blockedSum$ = this.searchParameters$.pipe(
            switchMap((value) => this.analyticsService.getBlockedPaymentsSum(value)),
            shareReplay(1)
        );
        this.chartDataX$ = this.searchParameters$.pipe(
            catchError(() => {
                return EMPTY;
            }),
            switchMap((value) => this.analyticsService.getSplitCountRatio(value)),
            filter((r) => !!r),
            shareReplay(1)
        );
        merge(
            this.currencies$,
            this.attemptedPayments$,
            this.blockedPayments$,
            this.ratioOfBlocked$,
            this.blockedSum$,
            this.chartDataX$,
            this.fraudSummary$
        ).subscribe((value) => this.finished$.next(value));
    }

    search(params: SearchBaseAnalyticsParams): void {
        this.searchParameters$.next(params);
    }
}
