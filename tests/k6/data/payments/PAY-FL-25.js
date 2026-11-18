export default {
  shops: [
    {
      role: 'blocked',
      template: `
        rule: blocked_user: userId() = "{{user}}" -> decline;
        rule: amount() >= 2000000000 -> decline;
      `,
      ready: { userId: '{{user}}', customerId: '{{customer}}', decision: 'fatal' },
    },
    {
      role: 'allowed',
      template: `
        rule: other_email: in("email", "nobody@k6.test") -> decline;
        rule: amount() >= 2000000000 -> decline;
      `,
      ready: { amount: 2000000000, decision: 'fatal' },
    },
    {
      role: 'amount',
      template: 'rule: by_amount: amount() > 0 -> decline;',
      ready: { decision: 'fatal' },
    },
  ],
  customer: {
    customerId: '{{customer}}',
    userId: '{{user}}',
    device: { ip: '10.0.0.1', fingerprint: '{{fingerprint}}' },
    contact: { email: '{{prefix}}@k6.test', phone: '70000000000' },
  },
  expected: { blockedShops: ['blocked'] },
  observedShop: 'amount',
};
