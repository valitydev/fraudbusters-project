export default {
  template: `
    rule: isTrusted(paymentsConditions(condition("RUB", 1, 1, 1))) -> accept;
    rule: big: amount() >= 100000 -> decline;
  `,
  declined: { amount: 150000 },
  expected: 'fatal',
};
