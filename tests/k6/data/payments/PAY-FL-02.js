export default {
  template: `
    rule: amount() >= 100000 -> decline;
    rule: amount() < 100000 -> accept;
  `,
  declined: { amount: 150000 },
  expected: { decision: 'fatal', resultStatus: 'decline' },
};
