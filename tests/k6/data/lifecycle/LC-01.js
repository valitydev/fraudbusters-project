export default {
  template: `
    rule: amount() >= 100000 -> decline;
    rule: amount() > 0 -> accept;
  `,
  declined: { amount: 150000 },
  expected: { declined: 'fatal', accepted: 'low' },
};
