export default {
  template: `
    rule: recurrent: isRecurrent() -> decline;
    rule: mobile: isMobile() -> highRisk;
    rule: other: amount() > 0 -> accept;
  `,
  scenarios: [
    { name: 'recurrent_payer_through_rest', payment: { payerType: 'recurrent' } },
    { name: 'mobile_payer_through_rest', payment: { payerType: 'mobile' } },
    { name: 'mobile_token_provider_through_rest', payment: { tokenMobileProvider: 'applepay' } },
    { name: 'ordinary payment', payment: {}, expected: 'accept/other' },
  ],
};
