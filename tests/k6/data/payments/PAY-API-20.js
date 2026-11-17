export default {
  emptyLoads: ['loadPaymentChanges', 'loadRefunds', 'loadChargebacks', 'loadFraudPayments'],
  invalidRecords: {
    payment_without_payment: { load: 'loadPaymentChanges', record: { paymentStatus: 'captured' } },
    refund_without_fields: { load: 'loadRefunds', record: { id: '{{refund}}' } },
    chargeback_without_fields: { load: 'loadChargebacks', record: { id: '{{chargeback}}' } },
  },
  expected: { empty: 201, invalid: 400 },
};
