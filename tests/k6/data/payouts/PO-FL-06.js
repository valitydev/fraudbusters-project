export default {
  count: {
    template: `
      rule: countPayouts("card_token", 24, hours, "party_id") > 2 -> decline;
      rule: amount() > 0 -> accept;
    `,
    loads: ['succeeded', { status: 'failed', errorCode: 'k6_error' }, 'pending', 'pending', 'succeeded',
      { status: 'failed', errorCode: 'k6_error' }],
  },
  sum: {
    template: `
      rule: sumPayouts("card_token", 24, hours, "party_id") > 15000 -> decline;
      rule: amount() > 0 -> accept;
    `,
    loads: [
      { status: 'pending', with: { amount: 30000 } },
      { status: 'succeeded', with: { amount: 10000 } },
      { status: 'failed', with: { amount: 10000 }, errorCode: 'k6_error' },
    ],
    expected: { afterPending: 'low', afterSucceededAndFailed: 'fatal' },
  },
};
