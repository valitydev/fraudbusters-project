export default {
  template: 'rule: amount() >= 2000000000 -> decline;',
  changed: 'rule: amount() > 1 -> accept;',
  invalid: 'rule: amount( > 10 -> accept;',
};
