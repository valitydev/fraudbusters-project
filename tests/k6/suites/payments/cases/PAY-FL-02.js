import data from '../../../data/payments/PAY-FL-02.js';
import { expectEqual, expectTrue } from '../../../lib/assert.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-02',
  title: 'Отклонение по сумме',
  profiles: ['functional', 'smoke'],
  run(context) {
    const steps = paymentSteps(context);
    const scope = steps.scope();
    const templateId = steps.applyTemplate('template', data.template, scope);
    const payment = steps.payment({ ...scope, ...data.declined });
    steps.expectDecision(payment, data.expected.decision, 'payment above the limit');
    const result = steps.awaitInspectResult(payment);
    expectEqual(result.checkedTemplate, templateId, 'checked template');
    expectEqual(String(result.resultStatus).toLowerCase(), data.expected.resultStatus, 'result status');
    expectTrue(Boolean(result.ruleChecked), 'checked rule is stored');
    context.observe('rule_checked', result.ruleChecked);
  },
};
