export default {
  template: `
    rule: uniquePayouts("user_id", "card_token", 24, hours) > 2 -> decline;
    rule: amount() > 0 -> accept;
  `,
  client: { userId: '{{user}}', customerId: '{{customer}}' },
  loads: [
    { status: 'succeeded', with: { cardToken: '{{card-1}}' } },
    { status: 'succeeded', with: { cardToken: '{{card-2}}' } },
    { status: 'succeeded', with: { cardToken: '{{card-3}}' } },
  ],
};
