export default {
  template: `
    rule: amount() >= 2000000000 -> decline;
    rule: by_party: inBlackList("party_id") -> decline;
    rule: by_wallet: inBlackList("wallet_id") -> decline;
    rule: by_currency: inBlackList("currency") -> decline;
    rule: by_user: inBlackList("user_id") -> decline;
    rule: by_named: inList("{{named-list}}", "email") -> highRisk;
    rule: by_geo: countryBy("ip") = "UNKNOWN" and amount() = 7301 -> highRisk;
    rule: amount() > 0 -> accept;
  `,
  ready: { amount: 2000000000, decision: 'fatal' },
  rows: [
    { type: 'black', name: 'PARTY_ID', value: '{{party-blocked}}', scope: {} },
    { type: 'black', name: 'WALLET_ID', value: '{{wallet-blocked}}', scope: { partyId: '{{party}}' } },
    { type: 'black', name: 'CURRENCY', value: 'USD', scope: { partyId: '{{party}}' } },
    { type: 'black', name: 'USER_ID', value: '{{user-blocked}}', scope: { partyId: '{{party}}' } },
    { type: 'naming', name: '{{named-list}}', value: '{{prefix}}-named@k6.test', scope: {} },
  ],
  scenarios: [
    { name: 'party_id', withdrawal: { partyId: '{{party-blocked}}' }, expected: 'fatal' },
    { name: 'wallet_id', withdrawal: { walletId: '{{wallet-blocked}}' }, expected: 'fatal' },
    { name: 'currency', withdrawal: { currency: 'USD' }, expected: 'fatal' },
    { name: 'user_id', withdrawal: { userId: '{{user-blocked}}', customerId: '{{customer}}' }, expected: 'fatal' },
    { name: 'named list', withdrawal: { email: '{{prefix}}-named@k6.test' }, expected: 'high' },
    { name: 'private ip has no country', withdrawal: { amount: 7301 }, expected: 'high' },
    { name: 'nothing listed', withdrawal: {}, expected: 'low' },
  ],
};
