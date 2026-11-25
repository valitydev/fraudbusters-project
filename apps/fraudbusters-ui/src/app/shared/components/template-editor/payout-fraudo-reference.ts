import { PAYMENT_FIELDS, PAYMENT_FRAUDO_SECTIONS } from './payment-fraudo-reference';

// The payout validator rejects payment-only predicates and 3-D Secure outcomes.
export const PAYOUT_SECTIONS = PAYMENT_FRAUDO_SECTIONS.map((section) => {
    if (section.title === 'Rule structure & logic') {
        return {
            ...section,
            snippets: section.snippets.map((snippet) =>
                snippet.code === 'NOT (isRecurrent())'
                    ? { ...snippet, code: 'NOT (inBlackList("card_token"))' }
                    : snippet
            ),
        };
    }
    if (section.title === 'Current payment') {
        return {
            title: 'Current payout',
            snippets: section.snippets
                .filter(({ code }) => !/^(isTrusted|isMobile|isRecurrent|paymentSystem|cardCategory)\(/.test(code))
                .map((snippet) => ({
                    ...snippet,
                    description: snippet.description.replace('payment', 'payout'),
                })),
        };
    }
    if (section.title === 'Payout history in payment rules') {
        return {
            ...section,
            title: 'Payout history',
            hint: 'Read completed payouts. Group by party_id or wallet_id to scope the history.',
        };
    }
    return section;
});

export const PAYOUT_FIELDS = [...PAYMENT_FIELDS.filter((field) => field !== 'shop_id'), 'wallet_id'];

export const PAYOUT_PRESETS = [
    { label: 'Empty', code: '' },
    { label: 'Black list check', code: 'rule: inBlackList("card_token") -> decline;' },
    { label: 'Amount limit', code: 'rule: amount() > 10000 -> decline;' },
    { label: 'Payout frequency', code: 'rule: countPayouts("user_id", 24, hours, "wallet_id") > 3 -> highRisk;' },
];

export const PAYOUT_EXAMPLES = [
    {
        label: 'Payout history scoped to a wallet',
        code: 'rule: repeat_payouts: countPayouts("user_id", 24, hours, "wallet_id") > 3 -> highRisk;',
    },
    {
        label: 'Successful payout amount in the same currency',
        code: 'rule: sumPayoutsSuccess("user_id", 1, calendar_days, "wallet_id", "currency") > 10000 -> decline catch: notify;',
    },
];
