export default {
  template: `
    rule: sum("fingerprint", 30, days, "party_id", "shop_id") > 20000 AND currency() = "RUB" -> decline;
    rule: amount() > 0 -> accept;
  `,
  loads: [
    { status: 'captured', with: { amount: 10000 } },
    { status: 'captured', with: { amount: 10000 } },
    { status: 'captured', with: { amount: 10000 } },
  ],
  otherCurrency: { currency: 'USD' },
  expected: { otherCurrency: 'low' },
};
