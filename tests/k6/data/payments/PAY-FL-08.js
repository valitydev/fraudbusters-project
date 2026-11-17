const withAccept = (rule) => `
  rule: ${rule} -> decline;
  rule: other: amount() > 0 -> accept;
`;

export default {
  variants: [
    {
      role: 'success',
      template: withAccept('sumSuccess("card_token", 30, days, "party_id") > 15000'),
      loads: [
        { status: 'captured', with: { amount: 10000 } },
        { status: 'captured', with: { amount: 10000 } },
        { status: 'failed', with: { amount: 30000 }, errorCode: 'k6_err' },
      ],
      expected: 'fatal',
    },
    {
      role: 'success-only-failed',
      template: withAccept('sumSuccess("card_token", 30, days, "party_id") > 15000'),
      loads: [
        { status: 'failed', with: { amount: 30000 }, errorCode: 'k6_err' },
        { status: 'captured', with: { amount: 10000 } },
      ],
      expected: 'low',
    },
    {
      role: 'error-code',
      template: withAccept('sumError("card_token", 30, days, "k6_err") > 15000'),
      loads: [
        { status: 'failed', with: { amount: 10000 }, errorCode: 'k6_err' },
        { status: 'failed', with: { amount: 10000 }, errorCode: 'k6_err' },
      ],
      expected: 'fatal',
    },
    {
      role: 'error-other-code',
      template: withAccept('sumError("card_token", 30, days, "k6_err") > 15000'),
      loads: [
        { status: 'failed', with: { amount: 10000 }, errorCode: 'k6_other' },
        { status: 'failed', with: { amount: 10000 }, errorCode: 'k6_other' },
      ],
      expected: 'low',
    },
    {
      role: 'error-any',
      template: withAccept('sumError("card_token", 30, days) > 15000'),
      loads: [
        { status: 'failed', with: { amount: 10000 }, errorCode: 'k6_other' },
        { status: 'failed', with: { amount: 10000 }, errorCode: 'k6_err' },
      ],
      expected: 'fatal',
    },
  ],
};
