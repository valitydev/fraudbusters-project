const byField = (field) => `
  rule: by_field: count("${field}", 1, days, "party_id", "shop_id") > 2 -> decline;
  rule: other: amount() > 0 -> accept;
`;

export default {
  loads: ['captured', 'captured', 'captured'],
  variants: [
    { role: 'email', template: byField('email'), payment: { email: '{{prefix}}-agg@k6.test' }, expected: 'fatal' },
    { role: 'ip', template: byField('ip'), payment: { ip: '10.31.0.7' }, expected: 'fatal' },
    { role: 'fingerprint', template: byField('fingerprint'), payment: { fingerprint: '{{agg-fingerprint}}' },
      expected: 'fatal' },
    { role: 'phone', template: byField('phone'), payment: { phone: '79990001122' }, expected: 'fatal' },
    { role: 'bin', template: byField('bin'), payment: { bin: '431999' }, expected: 'fatal' },
    { role: 'masked-pan', template: byField('masked_pan'), payment: { bin: '432999', lastDigits: '4321' } },
    { role: 'country-bank', template: byField('country_bank'), payment: { bankCountry: 'KAZ' } },
  ],
  maskedPanWithCatch: {
    template: `
      rule: by_field: count("masked_pan", 1, days) > 2 -> decline catch: highRisk;
      rule: other: amount() > 0 -> accept;
    `,
    payment: { bin: '433999', lastDigits: '4321' },
    ready: { bin: '433999', lastDigits: '4321', decision: 'high' },
  },
};
