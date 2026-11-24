import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';

import { BaseAnalyticsUtilService } from './services/base-analytics-util.service';
import { BaseAnalyticsService } from './services/base-analytics.service';
import { DateFormat } from '../../../../shared/constants/date-format';

@Component({
    templateUrl: 'base-analytics.component.html',
    styleUrls: ['./base-analytics.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BaseAnalyticsComponent {
    readonly colors = ['#a49bb7', '#26866c', '#586eaa', '#be4658'];
    currency = '';

    attemptedPayments$ = this.baseAnalyticsService.attemptedPayments$;
    blockedPayments$ = this.baseAnalyticsService.blockedPayments$;
    ratioOfBlocked$ = this.baseAnalyticsService.ratioOfBlocked$;
    blockedSum$ = this.baseAnalyticsService.blockedSum$;
    fraudSummary$ = this.baseAnalyticsService.fraudSummary$;
    chartData$ = this.baseAnalyticsService.chartDataX$;
    inProgress$ = this.baseAnalyticsService.inProgress$;

    constructor(
        private baseAnalyticsService: BaseAnalyticsService,
        private baseAnalyticsUtilService: BaseAnalyticsUtilService,
        protected datepipe: DatePipe
    ) {}

    search($event) {
        if ($event.type) {
            this.currency = $event.type;
            this.baseAnalyticsService.search({
                fromTime: this.datepipe.transform(
                    this.baseAnalyticsUtilService.todayFromTime($event.time).toUTCString(),
                    DateFormat._yyyyMMDdHHMmSs,
                    'UTC'
                ),
                toTime: this.datepipe.transform(new Date().toUTCString(), DateFormat._yyyyMMDdHHMmSs, 'UTC'),
                currency: $event.type,
                merchantId: $event.partyId,
                shopId: $event.shopId,
                splitUnit: this.baseAnalyticsUtilService.getSplitUnitFromTime($event.time),
            });
        }
    }
}
