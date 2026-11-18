export default {
  template: `
    rule: unique("ip", "email", 30, days, "party_id", "shop_id") > 1 -> decline;
    rule: amount() > 0 -> accept;
  `,
  ip: '10.0.6.1',
  newEmails: 3,
};
