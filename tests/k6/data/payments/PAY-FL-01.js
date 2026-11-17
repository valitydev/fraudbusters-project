export default {
  template: `
    rule: amount() >= 100000 -> decline;
    rule: amount() < 100000 -> accept;
  `,
  expected: {
    decision: 'low',
    history: { amount: 5000, currency: 'RUB', status: 'captured' },
    resultStatus: 'accept',
  },
};
