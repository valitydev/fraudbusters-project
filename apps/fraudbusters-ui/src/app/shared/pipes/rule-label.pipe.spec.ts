import { ruleLabel, unnamedRuleIndex } from './rule-label.pipe';

describe('rule labels', () => {
    it('recognises unnamed rules by their position', () => {
        expect(unnamedRuleIndex('0')).toBe('0');
        expect(unnamedRuleIndex('Rule #3')).toBe('3');
        expect(ruleLabel('0')).toBe('Rule #0');
    });

    it('keeps rule names', () => {
        expect(unnamedRuleIndex('amount_test')).toBeNull();
        expect(unnamedRuleIndex('rule2')).toBeNull();
        expect(ruleLabel('amount_test')).toBe('amount_test');
    });

    it('handles a missing rule', () => {
        expect(unnamedRuleIndex(undefined)).toBeNull();
        expect(ruleLabel(undefined)).toBe('');
    });
});
