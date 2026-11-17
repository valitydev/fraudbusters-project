export default {
  black: {
    template: `
      rule: inBlackList("card_token") -> decline;
      rule: amount() > 0 -> accept;
    `,
    expected: { beforeLists: 'low', listed: 'fatal', notListed: 'low', removed: 'low' },
  },
  white: {
    template: `
      rule: inWhiteList("email") -> accept;
      rule: amount() >= 2000000000 -> decline;
    `,
    ready: { amount: 2000000000, decision: 'fatal' },
    expected: { notListed: 'high', listed: 'low' },
  },
  grey: {
    template: `
      rule: inGreyList("card_token") -> accept;
      rule: amount() >= 2000000000 -> decline;
    `,
    ready: { amount: 2000000000, decision: 'fatal' },
    countInfo: { count: 1, fromMs: -60 * 1000, toMs: 24 * 3600 * 1000 },
    expected: { listed: 'low', afterLimit: 'high' },
  },
};
