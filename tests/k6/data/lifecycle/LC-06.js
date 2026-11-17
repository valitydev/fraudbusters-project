export default {
  appliesFrom: 'after-payouts',
  template: `
    rule: inBlackList("card_token") -> decline;
    rule: amount() > 0 -> accept;
  `,
  listedCard: '{{black-card}}',
  expected: { listedCard: 'fatal', otherCard: 'low' },
};
