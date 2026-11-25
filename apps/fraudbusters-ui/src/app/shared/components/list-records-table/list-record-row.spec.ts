import { listNamePill, parseRowInfo, windowStatus } from './list-record-row';

describe('list record rows', () => {
    it('reads the grey window from rowInfo, with timeToLive as its end', () => {
        const window = parseRowInfo(
            JSON.stringify({ count: 5, startCountTime: '2026-11-25T10:00:00Z', timeToLive: '2026-11-26T10:00:00Z' })
        );
        expect(window.count).toBe(5);
        expect(window.start.toISOString()).toBe('2026-11-25T10:00:00.000Z');
        expect(window.end.toISOString()).toBe('2026-11-26T10:00:00.000Z');
    });

    it('ignores empty or broken rowInfo', () => {
        expect(parseRowInfo('')).toBeUndefined();
        expect(parseRowInfo('{not json')).toBeUndefined();
        expect(parseRowInfo('{}')).toBeUndefined();
    });

    it('tells whether a window is scheduled, active or expired', () => {
        const now = new Date('2026-11-25T12:00:00Z');
        const at = (iso: string) => new Date(iso);
        expect(windowStatus({ start: at('2026-11-25T13:00:00Z') }, now)).toBe('scheduled');
        expect(windowStatus({ start: at('2026-11-25T10:00:00Z'), end: at('2026-11-25T11:00:00Z') }, now)).toBe(
            'expired'
        );
        expect(windowStatus({ start: at('2026-11-25T10:00:00Z'), end: at('2026-11-25T13:00:00Z') }, now)).toBe(
            'active'
        );
        expect(windowStatus(undefined, now)).toBe('active');
    });

    it('gives the same list name the same pill', () => {
        expect(listNamePill('EMAIL')).toBe(listNamePill('EMAIL'));
        expect(listNamePill('EMAIL')).toMatch(/^fb-pill-/);
    });
});
