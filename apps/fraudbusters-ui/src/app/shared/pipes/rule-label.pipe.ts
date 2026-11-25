import { Pipe, PipeTransform } from '@angular/core';

const UNNAMED_RULE = /^(?:Rule #)?(\d+)$/;

/**
 * Position of a rule without a name, or null for a named rule. Fraudo reports an unnamed
 * rule by its position in the template (RuleKeyGenerator); rule names cannot start with a
 * digit, so a numeric key is always an unnamed rule.
 */
export function unnamedRuleIndex(checkedRule: string | null | undefined): string | null {
    return UNNAMED_RULE.exec(checkedRule ?? '')?.[1] ?? null;
}

/** "Rule #N" for an unnamed rule, the rule name otherwise. */
export function ruleLabel(checkedRule: string | null | undefined): string {
    const index = unnamedRuleIndex(checkedRule);
    return index === null ? checkedRule ?? '' : `Rule #${index}`;
}

@Pipe({
    name: 'unnamedRuleIndex',
})
export class UnnamedRuleIndexPipe implements PipeTransform {
    transform(checkedRule: string | null | undefined): string | null {
        return unnamedRuleIndex(checkedRule);
    }
}

@Pipe({
    name: 'ruleLabel',
})
export class RuleLabelPipe implements PipeTransform {
    transform(checkedRule: string | null | undefined): string {
        return ruleLabel(checkedRule);
    }
}
