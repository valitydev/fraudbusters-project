import data from '../../../data/payments/PAY-FL-01.js';
import { expectEqual } from '../../../lib/assert.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-01',
  title: 'Платёж без риска: проверка, история, результат проверки',
  profiles: ['functional', 'smoke'],
  run(context) {
    const steps = paymentSteps(context);
    const scope = steps.scope();
    const templateId = steps.applyTemplate('template', data.template, scope);
    const payment = steps.payment(scope);
    steps.expectDecision(payment, data.expected.decision, 'payment below the limit');
    const [stored] = steps.loadAndAwait([payment]);
    expectEqual({ amount: stored.amount, currency: stored.currency, status: stored.status }, data.expected.history,
      'payment in history');
    const result = steps.awaitInspectResult(payment);
    expectEqual(result.checkedTemplate, templateId, 'checked template');
    expectEqual(String(result.resultStatus).toLowerCase(), data.expected.resultStatus, 'result status');
  },
};
