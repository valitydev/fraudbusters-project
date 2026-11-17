export default {
  functions: {
    template: `
      rule: by_user: userId() = "{{user}}" -> decline;
      rule: by_rand: rand(1) < 1 and amount() = 7101 -> decline;
      rule: by_decimal: amount() > 7101.5 and amount() < 7102.5 -> decline;
      rule: by_not_or: (not (amount() < 7103)) and (amount() = 7103 or amount() = 7104) -> decline;
      rule: by_neq: amount() != 7000 and amount() <= 7105 and amount() > 7104 -> highRisk;
      rule: by_like: like("card_token", ".*-like-card$") -> decline;
      rule: ps_lower: paymentSystem() = "visa" and amount() = 7106 -> decline;
      rule: ps_upper: paymentSystem() = "VISA" and amount() = 7106 -> decline;
      rule: category_known: cardCategory() != "UNKNOWN" and amount() = 7107 -> decline;
      rule: category_unknown: cardCategory() = "UNKNOWN" and amount() = 7107 -> decline;
      # a comment line is ignored
      rule: other: amount() > 0 -> accept;
    `,
    ready: { amount: 7101, decision: 'fatal' },
    scenarios: [
      { name: 'userId', payment: { userId: '{{user}}', customerId: '{{customer}}' }, expected: 'decline/by_user' },
      {
        name: 'other user', payment: { userId: '{{other-user}}', customerId: '{{customer}}' },
        expected: 'accept/other',
      },
      { name: 'rand(1) is always 0', payment: { amount: 7101 }, expected: 'decline/by_rand' },
      { name: 'decimal bounds', payment: { amount: 7102 }, expected: 'decline/by_decimal' },
      { name: 'not and or: 7103', payment: { amount: 7103 }, expected: 'decline/by_not_or' },
      { name: 'not and or: 7104', payment: { amount: 7104 }, expected: 'decline/by_not_or' },
      { name: 'not equal', payment: { amount: 7105 }, expected: 'high_risk/by_neq' },
      { name: 'like', payment: { cardToken: '{{prefix}}-like-card' }, expected: 'decline/by_like' },
      {
        name: 'like does not match a prefix', payment: { cardToken: '{{prefix}}-like-card-2' },
        expected: 'accept/other',
      },
      { name: 'payment system', payment: { amount: 7106 } },
      { name: 'card category', payment: { amount: 7107 } },
    ],
  },
  precedence: {
    template: `
      rule: not_precedence: not amount() < 7200 and amount() = 7201 -> decline;
      rule: other: amount() > 0 -> accept;
    `,
    ready: { amount: 7201, decision: 'fatal' },
    scenarios: [
      { name: 'not_without_parentheses_7300', payment: { amount: 7300 } },
      { name: 'not_without_parentheses_5000', payment: { amount: 5000 } },
    ],
  },
};
