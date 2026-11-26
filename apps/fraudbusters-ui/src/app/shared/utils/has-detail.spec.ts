import { hasDetail } from './has-detail';

describe('hasDetail', () => {
    it('hides empty, blank and placeholder values', () => {
        for (const value of [null, undefined, '', '   ', '-', ' - ', '—']) {
            expect(hasDetail(value)).withContext(JSON.stringify(value)).toBeFalse();
        }
    });

    it('shows real values, including zero and false', () => {
        for (const value of [0, false, 'RUB', 'xxxxx', '-1']) {
            expect(hasDetail(value)).withContext(JSON.stringify(value)).toBeTrue();
        }
    });
});
