export default {
  templates: {
    accept: 'rule: amount() > 0 -> accept;',
    decline: 'rule: amount() > 0 -> decline;',
    notify: 'rule: amount() > 0 -> notify;',
    noDecision: 'rule: amount() >= 2000000000 -> decline;',
  },
  expected: {
    onlyWalletTemplate: 'low',
    partyTemplateBeforeWalletTemplate: 'fatal',
    partyWithoutDecisionFallsThrough: 'low',
    partyTemplateForOtherWallet: 'fatal',
    partyGroupBeforeWalletGroup: 'fatal',
    walletGroupBeforePartyTemplate: 'low',
    notifyDoesNotStopCascade: 'fatal',
  },
};
