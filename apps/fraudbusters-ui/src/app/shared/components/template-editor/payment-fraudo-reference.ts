// Examples follow FraudoRules.g4 and its common, payment and payout imports.
// Field names follow the payment execution and history resolvers.
export const PAYMENT_FRAUDO_SECTIONS = [
    {
        title: 'Rule structure & logic',
        hint: 'Names are optional. Use AND, OR and NOT to combine conditions. Each rule ends with a semicolon.',
        snippets: [
            { code: 'rule: amount_limit: amount() > 10000 -> decline;', description: 'Named rule' },
            { code: 'AND', description: 'Require both conditions' },
            { code: 'OR', description: 'Require at least one condition' },
            { code: 'NOT (isRecurrent())', description: 'Negate a condition' },
            { code: 'catch: notify', description: 'Fallback after the outcome and before the semicolon' },
            { code: '# Explain the rule here', description: 'Comment to the end of the line' },
        ],
    },
    {
        title: 'Lists & matching',
        snippets: [
            { code: 'inBlackList("card_token")', description: 'Look up the current card token in the black list' },
            { code: 'inWhiteList("card_token")', description: 'Look up the current card token in the white list' },
            { code: 'inGreyList("email")', description: 'Look up the payment email in the grey list' },
            { code: 'inList("list_name", "email")', description: 'Replace list_name with a configured list name' },
            { code: 'in("country_bank", "RU", "KZ")', description: 'Match a field against several values' },
            { code: 'in(currency(), "RUB", "EUR")', description: 'Match any of the supplied values' },
            {
                code: 'like("email", ".*@example[.]test")',
                description: 'Match the entire value against a Java regular expression',
            },
        ],
    },
    {
        title: 'Current payment',
        snippets: [
            { code: 'amount() > 10000', description: 'Compare the amount in minor currency units' },
            { code: 'currency() = "RUB"', description: 'Compare the payment currency' },
            { code: 'userId() != "UNKNOWN"', description: 'Check for a merchant user ID' },
            { code: 'paymentSystem() = "VISA"', description: 'Compare the card payment system' },
            { code: 'cardCategory() = "credit"', description: 'Compare the card category' },
            { code: 'countryBy("ip") = "RU"', description: 'Resolve the country from the payment IP' },
            { code: 'countryBy("country_bank") = "RU"', description: 'Compare the card issuer country' },
            { code: 'isMobile()', description: 'Check whether the payment is mobile' },
            { code: 'isRecurrent()', description: 'Check whether the payment is recurrent' },
            { code: 'isTrusted()', description: 'Check whether the card is trusted' },
            {
                code: 'isTrusted("trusted-template")',
                description: 'Replace trusted-template with a configured trusted-tokens template',
            },
            { code: 'rand(100) < 10', description: 'Sample randomly. rand(100) returns an integer from 0 to 99.' },
        ],
    },
    {
        title: 'Payment history',
        hint: 'Add quoted grouping fields after the time window. For countError and sumError, the first string after the window is the error code.',
        snippets: [
            {
                code: 'count("card_token", 60, minutes, "party_id") > 3',
                description: 'Count payments by card token within a party',
            },
            { code: 'countSuccess("user_id", 24, hours, "party_id") > 3', description: 'Count successful payments' },
            { code: 'countPending("card_token", 1, hours) > 3', description: 'Count pending payments' },
            { code: 'countError("card_token", 1, hours) > 3', description: 'Count payments with any error' },
            {
                code: 'countError("card_token", 1, hours, "card_expired", "party_id") > 3',
                description: 'Count payments with a specific error code',
            },
            { code: 'countChargeback("card_token", 30, days) > 0', description: 'Count chargebacks' },
            { code: 'countRefund("card_token", 30, days) > 0', description: 'Count refunds' },
            {
                code: 'sum("card_token", 24, hours, "currency") > 10000',
                description: 'Sum payments in the current currency',
            },
            { code: 'sumSuccess("card_token", 24, hours, "currency") > 10000', description: 'Sum successful payments' },
            {
                code: 'sumError("card_token", 24, hours, "card_expired", "currency") > 10000',
                description: 'Sum failed payments with a specific error code',
            },
            { code: 'sumChargeback("card_token", 30, days, "currency") > 10000', description: 'Sum chargebacks' },
            { code: 'sumRefund("card_token", 30, days, "currency") > 10000', description: 'Sum refunds' },
            {
                code: 'unique("ip", "email", 24, hours, "party_id") > 3',
                description: 'Count distinct email values for the current IP and party',
            },
        ],
    },
    {
        title: 'Payout history in payment rules',
        hint: 'These functions read completed payout history. Use a shared field such as user_id and add party_id to scope history to the merchant.',
        snippets: [
            { code: 'countPayouts("user_id", 24, hours, "party_id") > 3', description: 'Count completed payouts' },
            {
                code: 'countPayoutsSuccess("user_id", 24, hours, "party_id") > 3',
                description: 'Count successful payouts',
            },
            { code: 'countPayoutsError("user_id", 24, hours) > 3', description: 'Count failed payouts' },
            {
                code: 'sumPayouts("user_id", 24, hours, "party_id", "currency") > 10000',
                description: 'Sum completed payouts in the current currency',
            },
            {
                code: 'sumPayoutsSuccess("user_id", 24, hours, "party_id", "currency") > 10000',
                description: 'Sum successful payouts',
            },
            {
                code: 'sumPayoutsError("user_id", 24, hours) > 10000',
                description: 'Sum failed payouts. An optional error code goes before grouping fields.',
            },
            {
                code: 'uniquePayouts("user_id", "currency", 24, hours, "party_id") > 1',
                description: 'Count distinct payout currencies for this user and party',
            },
        ],
    },
    {
        title: 'Time windows',
        hint: 'Insert after a history field. A number without a unit means hours. A second number sets the end offset.',
        snippets: [
            { code: ', 24', description: 'Last 24 hours using the default unit' },
            { code: ', 60, minutes', description: 'Last 60 minutes' },
            { code: ', 24, hours', description: 'Last 24 hours' },
            { code: ', 7, days', description: 'Last seven days' },
            { code: ', 24, 1, hours', description: 'From 24 hours ago to one hour ago' },
            { code: ', 1, calendar_days', description: 'Current calendar day' },
            { code: ', 1, calendar_months', description: 'From the start of the current calendar month' },
        ],
    },
];

export const PAYMENT_FIELDS = [
    'card_token',
    'bin',
    'last_digits',
    'email',
    'phone',
    'ip',
    'fingerprint',
    'user_id',
    'party_id',
    'shop_id',
    'currency',
    'country_bank',
];

export const PAYMENT_OUTCOMES = [
    { code: 'trust', description: 'Mark as trusted' },
    { code: 'accept', description: 'Accept the payment' },
    { code: '3ds', description: 'Request 3-D Secure' },
    { code: 'highRisk', description: 'Mark as high risk' },
    { code: 'decline', description: 'Decline the payment' },
    { code: 'notify', description: 'Notify and continue evaluating rules' },
    { code: 'declineAndNotify', description: 'Decline and notify' },
    { code: 'acceptAndNotify', description: 'Accept and notify' },
];

export const PAYMENT_CATCH_OUTCOMES = ['trust', 'accept', '3ds', 'highRisk', 'notify'];
