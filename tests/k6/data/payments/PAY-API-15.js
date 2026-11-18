export default {
  amounts: [5000, 5000, 150000, 5000],
  template: `
    rule: big_amount: amount() >= 100000 -> decline;
    rule: card_velocity: count("card_token", 1, days) > 2 -> decline;
    rule: other: amount() > 0 -> accept;
  `,
  expected: {
    bigAmount: 'decline/big_amount',
    otherDecisions: ['accept/other', 'decline/card_velocity'],
  },
};
