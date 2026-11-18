export default {
  variants: [
    {
      role: 'success',
      template: `
        rule: countSuccess("card_token", 30, days, "party_id") > 2 -> decline;
        rule: amount() > 0 -> accept;
      `,
      loads: ['captured', 'captured', 'captured', 'failed', 'pending'],
      expected: 'fatal',
    },
    {
      role: 'error',
      template: `
        rule: countError("card_token", 30, days) > 2 -> decline;
        rule: amount() > 0 -> accept;
      `,
      loads: ['failed', 'failed', 'failed', 'captured', 'pending'],
      expected: 'fatal',
    },
    {
      role: 'pending',
      template: `
        rule: countPending("card_token", 30, days) > 1 -> decline;
        rule: amount() > 0 -> accept;
      `,
      loads: ['pending', 'pending', 'pending', 'pending'],
      expected: 'fatal',
    },
    {
      role: 'error-other',
      template: `
        rule: countError("card_token", 30, days) > 2 -> decline;
        rule: amount() > 0 -> accept;
      `,
      loads: ['captured', 'captured', 'captured', 'failed', 'pending'],
      expected: 'low',
    },
  ],
};
