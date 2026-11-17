const EXPIRED = 'card_expired:k6';

export default {
  template: `
rule: allow_list: inWhiteList("email", "fingerprint", "ip") -> accept;
rule: deny_list: inBlackList("email", "fingerprint", "ip") -> decline;
rule: deny_domain: like("email", ".*@blocked-mail\\.test") -> decline;
rule: deny_domain: like("email", ".*@blocked-mail-alt\\.test") -> decline;

rule: loyal_by_count: amount() <= 500000 and (not in("email", "UNKNOWN")) and ((countSuccess("email", 3, days, "party_id") > 3 and countError("email", 3, days, "${EXPIRED}") < 3) or (countSuccess("email", 10, days, "party_id") > 6 and countError("email", 10, days, "${EXPIRED}") < 4)) -> trust;rule: loyal_by_count: ((countSuccess("email", 7, days, "party_id") > 9 and countError("email", 7, days, "${EXPIRED}") < 2) and not in("email", "UNKNOWN")) -> trust;

rule: loyal_by_sum: amount() <= 500000 and (not in("email", "UNKNOWN")) and sumSuccess("email", 10, days, "party_id") >= 300000 -> trust;

rule: regular_monthly: sumSuccess("email", 30, days, "party_id") >= 200000 and not in("email", "UNKNOWN") -> accept;

rule: pending_burst: countPending("email", 5, minutes, "party_id") > 2 and sumSuccess("email", 7, days, "party_id") < 100000 and not in("email", "UNKNOWN") -> decline;
rule: pending_hour: countPending("email", 90, minutes) > 8 and countError("email", 5, days, "${EXPIRED}") > 15 and not in("email", "UNKNOWN") -> decline;

rule: expired_day: countError("email", 1, days, "${EXPIRED}") > 3 and sumSuccess("email", 10, days, "party_id") < 300000 and not in("email", "UNKNOWN") -> decline;
rule: expired_two_weeks: countError("email", 14, days, "${EXPIRED}") > 6 and sumSuccess("email", 14, days, "party_id") < 250000 and not in("email", "UNKNOWN") -> decline;
`,
  ready: { client: 'probe', with: { email: '{{prefix}}@blocked-mail.test' }, decision: 'fatal' },
  lists: [
    { type: 'white', name: 'FINGERPRINT', value: '{{fingerprint-allowed}}' },
    { type: 'black', name: 'IP', value: '10.27.0.66' },
  ],
  listChecks: [
    {
      client: 'allowed',
      with: { email: '{{prefix}}-allowed@blocked-mail.test', fingerprint: '{{fingerprint-allowed}}' },
      decision: 'low',
    },
    { client: 'denied', with: { ip: '10.27.0.66' }, decision: 'fatal' },
  ],
  history: [
    { client: 'loyal', count: 5, status: 'captured' },
    { client: 'loyal-expired', count: 5, status: 'captured' },
    { client: 'loyal-other-code', count: 5, status: 'captured' },
    { client: 'unknown', count: 5, status: 'captured', with: { email: null } },
    { client: 'unknown-literal', count: 5, status: 'captured', with: { email: 'UNKNOWN' } },
    { client: 'loyal-sum', count: 2, status: 'captured', with: { amount: 200000 } },
    { client: 'loyal-expired', count: 4, status: 'failed', errorCode: EXPIRED },
    { client: 'loyal-other-code', count: 4, status: 'failed', errorCode: 'insufficient_funds' },
    { client: 'pending', count: 5, status: 'pending' },
  ],
  scenarios: [
    {
      name: 'allow list wins over the domain rule',
      client: 'allowed',
      with: { email: '{{prefix}}-allowed@blocked-mail.test', fingerprint: '{{fingerprint-allowed}}' },
      expected: 'accept/allow_list',
    },
    { name: 'black list by ip', client: 'denied', with: { ip: '10.27.0.66' }, expected: 'decline/deny_list' },
    {
      name: 'blocked domain', client: 'domain', with: { email: '{{prefix}}@blocked-mail.test' },
      expected: 'decline/deny_domain',
    },
    {
      name: 'second blocked domain with a repeated rule name', client: 'domain',
      with: { email: '{{prefix}}@blocked-mail-alt.test' }, expected: 'decline/deny_domain',
    },
    {
      name: 'escaped dot does not match any character', client: 'domain', with: { email: '{{prefix}}@blocked-mailxtest' },
      expected: 'three_ds/',
    },
    { name: 'newcomer without history', client: 'newcomer', expected: 'three_ds/' },
    { name: 'loyal by count', client: 'loyal', expected: 'trust/loyal_by_count' },
    { name: 'loyal by count above the amount limit', client: 'loyal', with: { amount: 600000 }, expected: 'three_ds/' },
    { name: 'expired errors with the rule code', client: 'loyal-expired', expected: 'decline/expired_day' },
    { name: 'errors with another code are not counted', client: 'loyal-other-code', expected: 'trust/loyal_by_count' },
    {
      name: 'payment without email is excluded as UNKNOWN', client: 'unknown', with: { email: null },
      expected: 'three_ds/',
    },
    { name: 'loyal by sum', client: 'loyal-sum', expected: 'trust/loyal_by_sum' },
    {
      name: 'regular monthly above the amount limit', client: 'loyal-sum', with: { amount: 600000 },
      expected: 'accept/regular_monthly',
    },
    { name: 'pending burst', client: 'pending', expected: 'decline/pending_burst' },
    { name: 'rest_answer_for_trust', client: 'loyal', observe: 'rest' },
    { name: 'literal_unknown_email_decision', client: 'unknown-literal', with: { email: 'UNKNOWN' }, observe: 'decision' },
  ],
};
