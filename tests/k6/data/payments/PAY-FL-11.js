export default {
  template: `
    rule: count("user_id", 1, hours, "party_id") > 2 -> decline;
    rule: amount() > 0 -> accept;
  `,
  client: { userId: '{{user}}', customerId: '{{customer}}' },
  loadsInFirstParty: ['captured', 'captured', 'captured'],
  expected: { firstParty: 'fatal', secondParty: 'low' },
};
