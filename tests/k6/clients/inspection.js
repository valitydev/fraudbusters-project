import { send } from '../lib/http.js';

export function inspectionClient(connection) {
  const base = connection.apiUrl;
  const call = (method, path, options) => send(connection, method, `${base}${path}`, options);
  return {
    inspectPayment: (payment) => call('POST', '/inspect-payment',
      { body: { payment }, stage: 'api_inspect_payment' }),
    loadPaymentChanges: (paymentsChanges, expectedStatuses) => call('POST', '/payments',
      { body: { paymentsChanges }, stage: 'api_load_payments', expectedStatuses }),
    loadRefunds: (refunds, expectedStatuses) => call('POST', '/refunds',
      { body: { refunds }, stage: 'api_load_refunds', expectedStatuses }),
    loadChargebacks: (chargebacks, expectedStatuses) => call('POST', '/chargebacks',
      { body: { chargebacks }, stage: 'api_load_chargebacks', expectedStatuses }),
    loadFraudPayments: (fraudPayments, expectedStatuses) => call('POST', '/fraud-payments',
      { body: { fraudPayments }, stage: 'api_load_fraud_payments', expectedStatuses }),
    inspectUser: (request, expectedStatuses) => call('POST', '/inspect-user',
      { body: request, stage: 'api_inspect_user', expectedStatuses }),
    loadWithdrawals: (withdrawals, expectedStatuses) => call('POST', '/withdrawals',
      { body: { withdrawals }, stage: 'api_load_withdrawals', expectedStatuses }),
  };
}
