export default {
  template: `
    rule: amount() >= 100000 -> decline;
    rule: amount() < 100000 -> accept;
  `,
  payments: [
    { with: {}, decision: 'low' },
    { with: { amount: 150000 }, decision: 'fatal' },
  ],
};
