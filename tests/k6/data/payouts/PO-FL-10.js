export default {
  forbidden: {
    is_trusted: 'rule: isTrusted() -> accept;',
    is_mobile: 'rule: isMobile() -> decline;',
    is_recurrent: 'rule: isRecurrent() -> decline;',
    result_3ds: 'rule: amount() > 0 -> 3ds;',
    catch_3ds: 'rule: rand(0) > 1 -> decline catch: 3ds;',
    shop_in_payout_history: 'rule: countPayouts("card_token", 1, days, "shop_id") > 1 -> decline;',
    wallet_in_payment_history: 'rule: count("wallet_id", 1, days) > 1 -> decline;',
    shop_in_list: 'rule: inBlackList("shop_id") -> decline;',
    shop_in_in: 'rule: in("shop_id", "k6") -> decline;',
    email_in_country_by: 'rule: countryBy("email") = "RUS" -> decline;',
  },
  savedForbidden: 'result_3ds',
  validForPayments: 'result_3ds',
};
