export default {
  variants: [
    {
      role: '3ds',
      template: 'rule: ask_3ds: amount() > 0 -> 3ds;',
      expected: { rest: 'high', stored: 'three_ds/ask_3ds' },
    },
    {
      role: 'accept-notify',
      template: 'rule: accept_notify: amount() > 0 -> acceptAndNotify;',
      expected: { rest: 'low', stored: 'accept_and_notify/accept_notify' },
    },
    {
      role: 'decline-notify',
      template: 'rule: decline_notify: amount() > 0 -> declineAndNotify;',
      expected: { rest: 'fatal', stored: 'decline_and_notify/decline_notify' },
    },
    {
      role: 'notify-chain',
      template: `
        rule: first_notice: amount() > 0 -> notify;
        rule: second_notice: amount() > 0 -> notify;
        rule: final_decline: amount() > 0 -> decline;
      `,
      expected: { rest: 'fatal', stored: 'decline/final_decline' },
    },
    {
      role: 'catch',
      template: `
        rule: broken: rand(0) > 1 -> decline catch: highRisk;
        rule: fallback: amount() > 0 -> accept;
      `,
      expected: { rest: 'high', stored: 'high_risk/broken' },
    },
    {
      role: 'no-catch',
      template: `
        rule: broken: rand(0) > 1 -> decline;
        rule: fallback: amount() > 0 -> accept;
      `,
      expected: { rest: 'low', stored: 'accept/fallback' },
    },
  ],
  invalidCatch: 'rule: amount() > 0 -> accept catch: decline;',
};
