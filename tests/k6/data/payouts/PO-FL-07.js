export default {
  count: {
    template: `
      rule: countPayoutsSuccess("card_token", 24, hours, "party_id") > 1 -> decline;
      rule: amount() > 0 -> accept;
    `,
    loads: [
      { status: 'failed', errorCode: 'k6_error' },
      { status: 'failed', errorCode: 'k6_error' },
      { status: 'failed', errorCode: 'k6_error' },
      'succeeded', 'succeeded', 'succeeded',
    ],
    expected: { afterFailed: ['low', 'low', 'low'] },
  },
  sum: {
    template: `
      rule: sumPayoutsSuccess("card_token", 24, hours, "party_id") > 15000 -> decline;
      rule: amount() > 0 -> accept;
    `,
    loads: [
      { status: 'failed', with: { amount: 30000 }, errorCode: 'k6_error' },
      { status: 'succeeded', with: { amount: 10000 } },
      { status: 'succeeded', with: { amount: 10000 } },
    ],
    expected: { afterFailed: 'low', afterTwoSucceeded: 'fatal' },
  },
};
