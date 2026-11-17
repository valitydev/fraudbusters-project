import data from '../../../data/payments/PAY-API-13.js';
import { expectSuccess } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';
import { waitFor } from '../../../lib/poll.js';
import { historyTime, historyWindow } from '../../../lib/time.js';
import { buildChargeback, buildRefund } from '../../../fixtures/payment.js';
import { awaitOperationsInHistory, CHARGEBACKS, REFUNDS } from '../operations.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-API-13',
  title: 'История возвратов, чарджбэков, fraud-платежей',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { management, inspection, ids, connection } = context;
    const scope = steps.scope();
    const fraud = ids.resolve(data.fraud);
    const payments = Array.from({ length: data.payments }, () => steps.payment(scope));
    steps.loadAndAwait(payments);
    const refunds = payments.map((payment) => buildRefund(payment, ids.unique('refund')));
    const chargebacks = payments.map((payment) => buildChargeback(payment, ids.unique('chargeback')));
    expectSuccess(inspection.loadRefunds(refunds), 'load refunds');
    expectSuccess(inspection.loadChargebacks(chargebacks), 'load chargebacks');
    const byCsv = payments.slice(0, data.fraudPaymentsByCsv);
    const byApi = payments[data.fraudPaymentsByCsv];
    const csv = [data.csvHeader, ...byCsv.map((payment) =>
      [payment.id, historyTime(new Date()), fraud.type, fraud.comment].join(','))].join('\n');
    expectSuccess(management.uploadFraudPaymentsCsv(csv), 'upload fraud payments CSV');
    const apiLoad = inspection.loadFraudPayments([{
      paymentId: byApi.id, eventTime: new Date().toISOString(), ...fraud,
    }], ANY_STATUS);
    context.observe('fraud_payment_api_status', apiLoad.status);
    awaitOperationsInHistory(context, REFUNDS, scope.partyId, refunds.map((refund) => refund.id));
    awaitOperationsInHistory(context, CHARGEBACKS, scope.partyId, chargebacks.map((chargeback) => chargeback.id));
    const findFraudPayment = (payment) => (expectSuccess(management.filterFraudPaymentsHistory(
      { ...historyWindow(), paymentId: payment.id, size: 10 }), 'filter fraud payments').result || [])
      .find((item) => item.payment && item.payment.id === payment.id);
    const stored = byCsv.map((payment) => waitFor(connection, `fraud payment ${payment.id}`, () => {
      const found = findFraudPayment(payment);
      return { done: Boolean(found), value: found };
    }));
    context.observe('fraud_payment_type_and_comment_match', stored[0].type === fraud.type
      && stored[0].comment === fraud.comment);
    context.observe('fraud_payment_api_stored', Boolean(findFraudPayment(byApi)));
  },
};
