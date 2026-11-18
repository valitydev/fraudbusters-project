const withAccept = (rule) => `
  rule: ${rule} -> decline;
  rule: amount() > 0 -> accept;
`;

export default {
  variants: [
    { role: 'count', template: withAccept('countRefund("card_token", 30, days, "party_id") > 0') },
    { role: 'sum', template: withAccept('sumRefund("card_token", 30, days, "party_id") > 1000') },
  ],
  expected: 'fatal',
};
