export default {
  template: `
    rule: inBlackList("card_token") -> decline;
    rule: amount() > 0 -> accept;
  `,
  list: { type: 'black', name: 'CARD_TOKEN' },
  expected: { listed: 'fatal', removed: 'low' },
};
