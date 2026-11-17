export default {
  template: 'rule: amount() >= 2000000000 -> decline;',
  cascade: ['group-template', 'party-template', 'shop-template'],
};
