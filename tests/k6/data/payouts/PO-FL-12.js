const withAccept = (rule) => `
  rule: ${rule} -> decline;
  rule: amount() > 0 -> accept;
`;

export default {
  variants: [
    {
      role: 'card',
      template: withAccept('count("card_token", 1, days, "party_id") > 1'),
      withdrawal: { cardToken: '{{card}}' },
      payments: [{ cardToken: '{{card}}' }, { cardToken: '{{card}}' }],
    },
    {
      role: 'email-sum',
      template: withAccept('sum("email", 1, days, "party_id") > 15000'),
      withdrawal: { email: '{{prefix}}-sum@k6.test' },
      payments: [{ email: '{{prefix}}-sum@k6.test', amount: 10000 }, { email: '{{prefix}}-sum@k6.test', amount: 10000 }],
    },
    {
      role: 'user-unique',
      template: withAccept('unique("user_id", "card_token", 1, days) > 1'),
      withdrawal: { userId: '{{user}}', customerId: '{{customer}}' },
      payments: [
        { userId: '{{user}}', customerId: '{{customer}}', cardToken: '{{card-1}}' },
        { userId: '{{user}}', customerId: '{{customer}}', cardToken: '{{card-2}}' },
      ],
    },
  ],
  expected: 'fatal',
};
