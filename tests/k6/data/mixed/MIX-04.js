export default {
  paymentTemplate: `
    rule: inBlackList("card_token") -> decline;
    rule: amount() >= 1500000000 -> decline;
    rule: amount() > 0 -> accept;
  `,
  payoutTemplate: `
    rule: inBlackList("card_token") -> decline;
    rule: amount() >= 1600000000 -> decline;
    rule: amount() > 0 -> accept;
  `,
  paymentOnlyAmount: 1500000000,
  expected: { paymentOnlyAmount: { payment: 'fatal', payout: 'low' }, ownList: 'fatal', otherList: 'low' },
};
