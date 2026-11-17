export default {
  templates: {
    accept: 'rule: amount() > 0 -> accept;',
    decline: 'rule: amount() > 0 -> decline;',
    noDecision: 'rule: amount() >= 2000000000 -> decline;',
  },
  expected: {
    partyGroupBeforePartyTemplate: 'fatal',
    shopGroupBeforePartyTemplate: 'low',
    bothPriorityOrders: ['fatal', 'low'],
    noDecisionFallsThroughInGroup: 'fatal',
  },
};
