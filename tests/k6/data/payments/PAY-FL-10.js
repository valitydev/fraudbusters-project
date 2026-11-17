const withAccept = (rule) => `
  rule: ${rule} -> decline;
  rule: amount() > 0 -> accept;
`;

export default {
  variants: [
    { role: 'count', template: withAccept('countChargeback("card_token", 30, days, "party_id") > 0') },
    { role: 'sum', template: withAccept('sumChargeback("card_token", 30, days, "party_id") > 1000') },
  ],
  expected: 'fatal',
};
