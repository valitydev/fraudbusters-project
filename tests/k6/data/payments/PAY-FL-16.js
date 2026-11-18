export default {
  template: `
    rule: isTrusted(paymentsConditions(condition("RUB", 1, 1, 1))) -> accept;
    rule: amount() >= 2000000000 -> decline;
  `,
  ready: { amount: 2000000000, decision: 'fatal' },
  loads: ['captured', 'captured'],
  expected: { withoutHistory: 'high', trusted: 'low' },
};
