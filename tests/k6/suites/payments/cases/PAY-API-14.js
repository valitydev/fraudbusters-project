import data from '../../../data/payments/PAY-API-14.js';
import { expectEqual, expectTrue } from '../../../lib/assert.js';
import { waitFor } from '../../../lib/poll.js';
import { inspectResultId, paymentSteps } from '../steps.js';

export default {
  id: 'PAY-API-14',
  title: 'История результатов проверки',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { ids, connection } = context;
    const scope = steps.scope();
    const templateId = steps.applyTemplate('template', data.template, scope);
    const inspected = data.payments.map((item) => {
      const payment = steps.payment({ ...scope, ...item.with });
      steps.expectDecision(payment, item.decision, `payment with decision ${item.decision}`);
      return payment;
    });
    const search = (query) => steps.inspectResults({ partyId: scope.partyId, ...query });
    inspected.forEach((payment) => waitFor(connection, `inspect result ${payment.id}`, () => {
      const found = search({ paymentId: inspectResultId(payment.id) }).length;
      return { done: found === 1, observed: found };
    }));
    context.observe('filter_invoice_id_count', search({ invoiceId: inspected[0].id }).length);
    context.observe('filter_plain_payment_id_count', search({ paymentId: inspected[0].id }).length);
    const byTemplate = search({ template: templateId });
    expectTrue(byTemplate.length >= inspected.length && byTemplate.every((item) => item.checkedTemplate === templateId),
      `filter by template returns only its results: ${byTemplate.length}`);
    expectEqual(search({ template: ids.name('other-template') }).length, 0, 'filter by another template');
  },
};
