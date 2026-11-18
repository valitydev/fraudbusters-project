export default {
  template: 'rule: amount() >= 2000000000 -> decline;',
  commandTypes: ['CREATE', 'DELETE'],
  objectTypes: ['template', 'reference'],
  expectedRecords: ['template:CREATE', 'template:DELETE', 'reference:CREATE'],
};
