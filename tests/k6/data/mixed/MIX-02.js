export default {
  payoutTemplate: `
    rule: countSuccess("user_id", 30, days, "party_id") < 1 -> decline;
    rule: amount() > 0 -> accept;
  `,
  client: { userId: '{{user}}', customerId: '{{customer}}' },
  expected: { withoutPayments: 'fatal', afterCapturedPayment: 'low' },
};
