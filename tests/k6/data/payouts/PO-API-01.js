export default {
  valid: 'rule: amount() >= 2000000000 -> decline;',
  withPayoutHistory: `
    rule: countPayouts("card_token", 24, hours, "party_id") > 2
      AND sumPayoutsSuccess("user_id", 24, hours) > 1000
      AND countPayoutsError("card_token", 24, hours, "k6_limit") > 0 -> decline;
  `,
  invalid: 'rule: amount( > 10 -> accept;',
  spacedErrorCode: 'rule: countPayoutsError("card_token", 24, hours, "k6: limit") > 0 -> decline;',
};
