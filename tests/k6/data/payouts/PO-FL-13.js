export default {
  template: `
    rule: countPayouts("card_token", 24, hours, "wallet_id") > 1 -> decline;
    rule: amount() > 0 -> accept;
  `,
  loadsInFirstWallet: ['succeeded', 'succeeded'],
  expected: { firstWallet: 'fatal', secondWallet: 'low' },
};
