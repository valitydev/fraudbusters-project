export default {
  template: `
    rule: inWhiteList("email") -> accept;
    rule: amount() >= 2000000000 -> decline;
  `,
  ready: { amount: 2000000000, decision: 'fatal' },
  email: '{{prefix}}-white@k6.test',
  list: { type: 'white', name: 'EMAIL' },
  expected: { notListed: 'high', listed: 'low' },
};
