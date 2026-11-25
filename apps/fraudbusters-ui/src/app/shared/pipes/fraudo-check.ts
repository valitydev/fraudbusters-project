import { FRAUDO_FIELD_ARGUMENTS } from './fraudo-field-arguments';
import {
    FRAUDO_CATCH_RESULTS,
    FRAUDO_FUNCTIONS,
    FRAUDO_PAYMENT_FIELDS,
    FRAUDO_PAYOUT_FIELDS,
    FRAUDO_RESULTS,
} from './fraudo-grammar.generated';
import { FraudoToken } from './fraudo-tokens.pipe';

const COMPLETE_STRING = /^"(?:[^"\r\n]|"")*"$/;

/** Payment and payout rules resolve different fields. */
export type FraudoDialect = 'payment' | 'payout';

const FIELDS: Record<FraudoDialect, { own: ReadonlySet<string>; other: ReadonlySet<string> }> = {
    payment: { own: FRAUDO_PAYMENT_FIELDS, other: FRAUDO_PAYOUT_FIELDS },
    payout: { own: FRAUDO_PAYOUT_FIELDS, other: FRAUDO_PAYMENT_FIELDS },
};

// Static payout restrictions of the fraudo PayoutTemplateValidator.
const NOT_FOR_PAYOUTS = new Set(['3ds', 'isTrusted', 'isMobile', 'isRecurrent']);

interface OpenBracket {
    index: number;
    /** The function this bracket calls, if any. */
    fn?: string;
    /** String arguments of that call seen so far; a nested call counts as one. */
    strings: number;
}

export interface FraudoCheck {
    /** Token index of each paired bracket mapped to the index of its pair, both ways. */
    pairs: Map<number, number>;
}

/**
 * Lightweight checks that help while typing, ahead of server validation. Problems are
 * written to `token.problem`. The grammar rule is `rule: [name:] expression -> result
 * [catch: catch_result];`; each rule is checked on its own, so a mistake in one rule is
 * reported there and not in the rules after it.
 */
export function checkFraudo(tokens: FraudoToken[], dialect: FraudoDialect = 'payment'): FraudoCheck {
    const fields = FIELDS[dialect];
    const pairs = new Map<number, number>();
    let open: OpenBracket[] = [];
    let inRule = false;
    let arrow: FraudoToken | undefined;
    let expecting: 'result' | 'catch' | null = null;
    let expectedAfter: FraudoToken | undefined;
    let last: FraudoToken | undefined;

    const flag = (token: FraudoToken, problem: string) => (token.problem ??= problem);
    const closeBrackets = () => {
        open.forEach(({ index }) => flag(tokens[index], 'Unclosed "("'));
        open = [];
    };
    const endRule = (end: FraudoToken | undefined) => {
        closeBrackets();
        if (expecting && expectedAfter) {
            flag(expectedAfter, `Missing result after "${expectedAfter.text}"`);
        }
        if (end && inRule && !arrow) {
            flag(end, 'Rule has no "->" result');
        }
        inRule = false;
        arrow = undefined;
        expecting = null;
    };

    tokens.forEach((token, index) => {
        const { text, kind } = token;
        if (!text.trim() || (kind === 'plain' && text.startsWith('#'))) {
            return;
        }
        if (expecting) {
            const allowed = expecting === 'result' ? FRAUDO_RESULTS : FRAUDO_CATCH_RESULTS;
            if (/^\w+$/.test(text)) {
                if (dialect === 'payout' && NOT_FOR_PAYOUTS.has(text)) {
                    flag(token, `"${text}" is not supported for payouts`);
                } else if (!allowed.has(text)) {
                    flag(
                        token,
                        expecting === 'catch' && FRAUDO_RESULTS.has(text)
                            ? `"${text}" is not allowed after "catch:"`
                            : `Unknown result "${text}"`
                    );
                }
                expecting = null;
                last = token;
                return;
            }
            if (expectedAfter) {
                flag(expectedAfter, `Missing result after "${expectedAfter.text}"`);
            }
            expecting = null;
        }

        if (kind === 'keyword' && text === 'rule:') {
            const previousRuleOpen = inRule;
            endRule(undefined);
            if (previousRuleOpen && last) {
                flag(last, 'Missing ";"');
            }
            inRule = true;
        } else if (kind === 'keyword' && (text === '->' || text === 'catch:')) {
            arrow ??= text === '->' ? token : undefined;
            expecting = text === '->' ? 'result' : 'catch';
            expectedAfter = token;
        } else if (kind === 'punctuation' && text === '(') {
            open.push({ index, fn: last?.kind === 'function' ? last.text : undefined, strings: 0 });
        } else if (kind === 'punctuation' && text === ')') {
            const pair = open.pop();
            if (pair === undefined) {
                flag(token, 'Unexpected ")"');
            } else {
                pairs.set(pair.index, index).set(index, pair.index);
                // in(countryBy("bin"), "RUS"): the nested call takes the place of the field.
                const parent = open[open.length - 1];
                if (pair.fn && parent) {
                    parent.strings += 1;
                }
            }
        } else if (kind === 'punctuation' && text === ';') {
            endRule(token);
            last = token;
            return;
        } else if (kind === 'function' && !FRAUDO_FUNCTIONS.has(text)) {
            flag(token, `Unknown function "${text}"`);
        } else if (kind === 'function' && dialect === 'payout' && NOT_FOR_PAYOUTS.has(text)) {
            flag(token, `"${text}" is not supported for payouts`);
        } else if (kind === 'string' && !COMPLETE_STRING.test(text)) {
            flag(token, 'Unclosed string');
        } else if (kind === 'string') {
            const call = open[open.length - 1];
            const isField = call?.fn && FRAUDO_FIELD_ARGUMENTS[call.fn]?.(call.strings);
            if (call) {
                call.strings += 1;
            }
            const field = text.slice(1, -1).replace(/""/g, '"');
            if (isField && !fields.own.has(field)) {
                flag(
                    token,
                    fields.other.has(field)
                        ? `Field "${field}" is not available in ${dialect} rules`
                        : `Unknown field "${field}"`
                );
            }
        }
        last = token;
    });

    const lastRuleOpen = inRule;
    endRule(undefined);
    if (lastRuleOpen && last) {
        flag(last, 'Missing ";"');
    }
    return { pairs };
}

/** Status text for the problems found by `checkFraudo`, or '' when there are none. */
export function fraudoProblemsMessage(tokens: FraudoToken[]): string {
    let line = 1;
    let first: { problem: string; line: number } | undefined;
    let count = 0;
    for (const token of tokens) {
        if (token.problem) {
            count += 1;
            first ??= { problem: token.problem, line };
        }
        line += token.text.split('\n').length - 1;
    }
    if (!first) {
        return '';
    }
    const where = `${first.problem} on line ${first.line}`;
    return count > 1 ? `${count} problems, first: ${where}` : where;
}
