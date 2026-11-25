import { FRAUDO_FIELD_ARGUMENTS } from './fraudo-field-arguments';
import { FRAUDO_FUNCTIONS } from './fraudo-grammar.generated';

describe('FRAUDO_FIELD_ARGUMENTS', () => {
    it('describes every function of the grammar', () => {
        expect([...FRAUDO_FUNCTIONS].filter((fn) => !FRAUDO_FIELD_ARGUMENTS[fn])).toEqual([]);
        expect(Object.keys(FRAUDO_FIELD_ARGUMENTS).filter((fn) => !FRAUDO_FUNCTIONS.has(fn))).toEqual([]);
    });
});
