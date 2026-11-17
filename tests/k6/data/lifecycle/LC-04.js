export default {
  template: `
    rule: count("card_token", 30, days, "party_id") > 2 -> decline;
    rule: amount() > 0 -> accept;
  `,
  loads: ['captured', 'captured', 'captured'],
  expected: 'fatal',
};
