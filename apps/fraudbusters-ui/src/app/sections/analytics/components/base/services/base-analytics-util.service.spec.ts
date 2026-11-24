import { BaseAnalyticsUtilService } from './base-analytics-util.service';

describe('Analytics date ranges', () => {
    const service = new BaseAnalyticsUtilService();
    const now = new Date('2026-11-24T11:30:00Z');

    it('includes today and the preceding six UTC days in the seven-day preset', () => {
        const range = service.resolveRange('7-days', '', '', now);
        expect(range.from.toISOString()).toBe('2026-11-18T00:00:00.000Z');
        expect(range.to).toEqual(now);
        expect(range.splitUnit).toBe('day');
    });

    it('uses only the previous UTC calendar day for Yesterday', () => {
        const range = service.resolveRange('yesterday', '', '', now);
        expect(range.from.toISOString()).toBe('2026-11-23T00:00:00.000Z');
        expect(range.to.toISOString()).toBe('2026-11-23T23:59:59.999Z');
        expect(range.splitUnit).toBe('hour');
    });

    it('keeps a custom end on today within the elapsed part of the day', () => {
        const range = service.resolveRange('custom', '2026-11-24', '2026-11-24', now);
        expect(range.from.toISOString()).toBe('2026-11-24T00:00:00.000Z');
        expect(range.to).toEqual(now);
        expect(range.splitUnit).toBe('hour');
    });

    it('rejects invalid calendar dates, future dates, and reversed ranges', () => {
        expect(service.resolveRange('custom', '2026-10-32', '2026-11-18', now)).toBeNull();
        expect(service.resolveRange('custom', '2026-11-20', '2026-11-25', now)).toBeNull();
        expect(service.resolveRange('custom', '2026-11-20', '2026-11-19', now)).toBeNull();
    });
});
