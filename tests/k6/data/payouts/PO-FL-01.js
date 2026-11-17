export default {
  template: `
    rule: amount() >= 100000 -> decline;
    rule: amount() < 100000 -> accept;
  `,
  declined: { amount: 150000 },
  expected: {
    decision: 'low',
    history: { amount: 5000, currency: 'RUB', status: 'succeeded' },
    declined: 'fatal',
  },
};
