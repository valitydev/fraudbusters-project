export default {
  paymentTemplate: `
    rule: inBlackList("card_token") -> decline;
    rule: count("card_token", 1, hours, "party_id", "shop_id") > 2 -> decline;
    rule: amount() >= 100000 -> decline;
    rule: amount() > 0 -> accept;
  `,
  payoutTemplate: `
    rule: inBlackList("card_token") -> decline;
    rule: amount() >= 100000 -> decline;
    rule: amount() > 0 -> accept;
  `,
  velocityHistory: ['captured', 'captured', 'captured'],
  flows: {
    accept: { decision: 'low', loaded: true },
    amount: { decision: 'fatal', with: { amount: 150000 } },
    black: { decision: 'fatal', card: 'black' },
    velocity: { decision: 'fatal', card: 'velocity' },
    payout_accept: { decision: 'low', loaded: true, payout: true },
    payout_black: { decision: 'fatal', card: 'black', payout: true },
  },
  shares: {
    payments: { accept: 70, amount: 10, black: 10, velocity: 10, payout_accept: 0, payout_black: 0 },
    mixed: { accept: 50, amount: 10, black: 10, velocity: 10, payout_accept: 15, payout_black: 5 },
  },
  historySearchShare: 0.05,
  reconfiguration: { rule: 'rule: k6_reconfigured: amount() = 4242 -> decline;', amount: 4242, decision: 'fatal' },
};
