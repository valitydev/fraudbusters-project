export default {
  templates: {
    accepts: 'rule: amount() > 0 -> accept;',
    declines: 'rule: amount() > 0 -> decline;',
  },
  expected: {
    accepts: 'low',
    changedDeclines: 'fatal',
    referenceRemoved: 'high',
    referenceRestored: 'fatal',
    templateRemoved: 'high',
  },
};
