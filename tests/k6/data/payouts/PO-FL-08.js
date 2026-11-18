const withAccept = (rule) => `
  rule: ${rule} -> decline;
  rule: amount() > 0 -> accept;
`;

export default {
  variants: [
    { role: 'code', template: withAccept('countPayoutsError("card_token", 24, hours, "k6_limit") > 1') },
    { role: 'any', template: withAccept('countPayoutsError("card_token", 24, hours) > 1') },
    { role: 'sum-code', template: withAccept('sumPayoutsError("card_token", 24, hours, "k6_limit") > 15000') },
  ],
  loads: [
    { status: 'failed', with: { amount: 10000 }, errorCode: 'k6_other' },
    { status: 'failed', with: { amount: 10000 }, errorCode: 'k6_other' },
    { status: 'failed', with: { amount: 10000 }, errorCode: 'k6_limit' },
    { status: 'failed', with: { amount: 10000 }, errorCode: 'k6_limit' },
    'succeeded',
  ],
  expected: {
    otherCodeIsNotCounted: ['low', 'low'],
    anyCodeAfterTwoErrors: 'fatal',
  },
};
