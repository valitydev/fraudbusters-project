export default {
  shopTemplate: 'rule: amount() > 0 -> accept;',
  partyTemplates: {
    declines: 'rule: amount() > 0 -> decline;',
    noDecision: 'rule: amount() >= 2000000000 -> decline;',
  },
  expected: {
    onlyShopTemplate: 'low',
    partyTemplateFirst: 'fatal',
    partyWithoutDecisionFallsThrough: 'low',
    partyTemplateForOtherShop: 'fatal',
  },
};
