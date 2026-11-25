import { fraudoValidationLines } from './fraudo-validation-lines';

describe('Fraudo validation line positions', () => {
    it('recognizes parser and semantic diagnostics and deduplicates source lines', () => {
        expect(fraudoValidationLines('line 12:4 invalid token\nline 2: invalid field\nline 12:9 missing ")"')).toEqual([
            2, 12,
        ]);
    });

    it('ignores diagnostics without a source line', () => {
        expect(fraudoValidationLines('No errors\nExpected token near line 9\nline 0: internal error')).toEqual([]);
    });
});
