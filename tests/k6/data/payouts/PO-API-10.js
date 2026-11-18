export default {
  template: 'rule: amount() >= 2000000000 -> decline;',
  commandTypes: ['CREATE', 'DELETE'],
  objectTypes: ['payout_template', 'payout_reference', 'payout_group', 'payout_group_reference', 'payout_black'],
  expectedRecords: ['payout_template:CREATE', 'payout_template:DELETE', 'payout_reference:CREATE',
    'payout_group:CREATE', 'payout_group_reference:CREATE', 'payout_black:CREATE'],
};
