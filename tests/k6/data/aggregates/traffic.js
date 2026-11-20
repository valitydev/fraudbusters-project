// Traffic of the aggregates suite. History comes from seed.sql (scripts/run-k6.sh seed), its shape from shape.json.
export default {
  template: `
    rule: black: inBlackList("card_token") -> decline;
    rule: week_success: countSuccess("card_token", 7, days, "party_id") > 40 -> decline;
    rule: hour_errors: countError("card_token", 1, hours, "card_expired", "party_id", "shop_id") > 5 -> decline;
    rule: hour_count: count("card_token", 1, hours, "party_id", "shop_id") > 10 -> decline;
    rule: week_funds: countError("email", 7, days, "insufficient_funds", "party_id") > 30 -> decline;
    rule: week_emails: unique("card_token", "email", 7, days, "party_id") > 4 -> highRisk;
    rule: hour_fingerprints: unique("fingerprint", "card_token", 1, hours, "party_id", "shop_id") > 5 -> highRisk;
    rule: big_amount: amount() >= 100000 -> decline;
    rule: default_accept: amount() > 0 -> accept;
  `,
  // Probes: own cards in the first seeded shop with a history that triggers one rule each.
  probes: {
    black: { decision: 'fatal' },
    velocity: { decision: 'fatal', history: { count: 11, status: 'captured' }, refill: 'captured' },
    errors: { decision: 'fatal', history: { count: 6, status: 'failed', errorCode: 'card_expired' },
      refill: 'failed' },
    weekly: { decision: 'fatal', history: { count: 41, status: 'captured' } },
    emails: { decision: 'high', history: { count: 5, status: 'captured', distinctEmails: true } },
  },
  flows: {
    seeded: { decisions: ['low', 'high', 'fatal'] },
    accept: { decision: 'low', loaded: true },
    amount: { decision: 'fatal', amount: 150000 },
    black: { probe: 'black' },
    velocity: { probe: 'velocity' },
    errors: { probe: 'errors' },
    weekly: { probe: 'weekly' },
    emails: { probe: 'emails' },
  },
  shares: { seeded: 60, accept: 20, amount: 4, black: 4, velocity: 4, errors: 3, weekly: 3, emails: 2 },
  // Status of a seeded payment loaded after its check.
  seededLoad: { failedShare: 0.25 },
  errorCodes: ['insufficient_funds', 'insufficient_funds', 'card_expired', 'rejected_by_issuer',
    'preauthorization_failed', 'timeout'],
  historySearchShare: 0.05,
  reconfiguration: { rule: 'rule: k6_reconfigured: amount() = 4242 -> decline;', amount: 4242, decision: 'fatal' },
};
