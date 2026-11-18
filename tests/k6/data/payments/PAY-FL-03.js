export default {
  template: `
    rule: amount() >= 2000000000 -> decline;
  `,
  ready: { amount: 2000000000, decision: 'fatal' },
  expected: { withoutReferences: 'high', noRuleMatched: 'high' },
};
