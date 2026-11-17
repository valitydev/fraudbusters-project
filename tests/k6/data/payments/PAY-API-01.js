export default {
  valid: 'rule: amount() >= 2000000000 -> decline;',
  invalid: 'rule: amount( > 10 -> accept;',
  rejected: [
    {
      name: 'error code with a space after the colon',
      template: 'rule: countError("card_token", 1, days, "card_expired: k6") > 1 -> decline;',
      mentions: 'card_expired: k6',
    },
    {
      name: 'payment history over a field it does not store',
      template: 'rule: count("masked_pan", 1, days, "party_id") > 2 -> decline;',
      mentions: 'masked_pan',
    },
  ],
};
