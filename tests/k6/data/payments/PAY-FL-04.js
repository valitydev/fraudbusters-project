export default {
  template: `
    rule: count("card_token", 30, days, "party_id", "shop_id") > 2 -> decline;
    rule: amount() > 0 -> accept;
  `,
  loadsInOtherShop: ['captured', 'captured', 'captured', 'captured'],
  loadsInOwnShop: ['captured', 'captured', 'captured', 'captured'],
  expected: { otherShop: 'low' },
};
