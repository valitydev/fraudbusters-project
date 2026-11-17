export default {
  paymentTemplateWithPayoutFunctions: `
    rule: countPayouts("card_token", 24, hours) > 1
      AND sumPayoutsSuccess("user_id", 24, hours, "party_id") > 1000
      AND countPayoutsError("card_token", 24, hours, "k6_limit") > 0 -> decline;
  `,
};
