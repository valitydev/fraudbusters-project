export default {
  paymentTemplate: `
    rule: countPayoutsSuccess("user_id", 24, hours, "party_id") > 3 -> highRisk;
    rule: amount() > 0 -> accept;
  `,
  client: { userId: '{{user}}', customerId: '{{customer}}' },
  succeededPayouts: ['succeeded', 'succeeded', 'succeeded', 'succeeded'],
  expected: { afterPayouts: 'high', otherParty: 'low' },
};
