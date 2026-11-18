export default {
  partyTemplate: 'rule: amount() > 0 -> notify;',
  shopTemplate: 'rule: amount() > 0 -> decline;',
  expected: { shopDecisionAfterPartyNotify: 'fatal', onlyNotify: 'high' },
};
