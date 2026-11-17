export default {
  defaultTemplate: 'rule: amount() > 0 -> decline;',
  expected: { firstPaymentOfNewShop: 'high', afterReferenceCreated: 'fatal' },
};
