export default {
  chargeback: {
    amount: 5000,
    currency: 'RUB',
    status: 'accepted',
    category: 'fraud',
    chargebackCode: 'k6',
    payerType: 'payment_resource',
  },
  client: { email: '{{prefix}}-candidate@k6.test', fingerprint: '{{candidate-fingerprint}}' },
  expected: { email: 'EMAIL', ip: 'IP', fingerprint: 'FINGERPRINT', listType: 'black' },
  approve: ['email', 'fingerprint'],
};
