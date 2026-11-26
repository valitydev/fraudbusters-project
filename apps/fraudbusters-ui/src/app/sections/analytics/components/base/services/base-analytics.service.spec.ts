import { of, Subject } from 'rxjs';

import { AnalyticsState, BaseAnalyticsService } from './base-analytics.service';
import { AnalyticsService } from '../../../../../api/payments/analytics';

describe('BaseAnalyticsService', () => {
    const params = {
        fromTime: '2026-11-19 00:00:00',
        toTime: '2026-11-25 23:59:59',
        currency: 'RUB',
        splitUnit: 'day',
    };

    it('keeps the previous data on screen while the next selection loads', () => {
        const attempted = new Subject<number>();
        const analytics = jasmine.createSpyObj<AnalyticsService>('AnalyticsService', [
            'getCurrencies',
            'getAttemptedPaymentsCount',
            'getBlockedPaymentsCount',
            'getBlockedPaymentsRatio',
            'getBlockedPaymentsSum',
            'getSplitCountRatio',
            'getSummary',
        ]);
        analytics.getCurrencies.and.returnValue(of(['RUB']));
        analytics.getAttemptedPaymentsCount.and.callFake(() => attempted);
        analytics.getBlockedPaymentsCount.and.returnValue(of(0));
        analytics.getBlockedPaymentsRatio.and.returnValue(of(0));
        analytics.getBlockedPaymentsSum.and.returnValue(of(0));
        analytics.getSplitCountRatio.and.returnValue(of({ currency: 'RUB', series: [] }));
        analytics.getSummary.and.returnValue(of([]));

        const service = new BaseAnalyticsService(analytics);
        const states: AnalyticsState[] = [];
        service.state$.subscribe((state) => states.push(state));

        service.search(params);
        expect(states[states.length - 1]).toEqual({ loading: true, data: undefined });
        attempted.next(4);
        attempted.complete();
        const loaded = states[states.length - 1];
        expect(loaded.loading).toBeFalsy();
        expect(loaded.data?.attempted).toBe(4);

        service.search({ ...params, currency: 'USD' });
        expect(states[states.length - 1]).toEqual({ loading: true, data: loaded.data });
    });
});
