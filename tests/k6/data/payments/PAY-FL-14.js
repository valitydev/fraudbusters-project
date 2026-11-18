export default {
  template: `
    rule: inGreyList("card_token") -> accept;
    rule: amount() >= 2000000000 -> decline;
  `,
  ready: { amount: 2000000000, decision: 'fatal' },
  list: { type: 'grey', name: 'CARD_TOKEN' },
  countInfo: { count: 1, fromMs: -60 * 1000, toMs: 24 * 3600 * 1000 },
  expected: { notListed: 'high', listed: 'low', afterLimit: 'high' },
};
