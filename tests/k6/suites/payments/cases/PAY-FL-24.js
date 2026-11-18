import data from '../../../data/payments/PAY-FL-24.js';
import { expectSuccess } from '../../../lib/assert.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-24',
  title: 'Изменение и удаление шаблона, удаление привязки',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { management } = context;
    const { expected } = data;
    const scope = steps.scope();
    const next = () => steps.payment(scope);
    const templateId = steps.applyTemplate('template', data.templates.accepts, scope, { decision: expected.accepts });
    const [referenceId] = steps.awaitReferenceIds(templateId, 1);
    steps.saveTemplate(templateId, data.templates.declines);
    steps.awaitDecision('changed template declines', next, expected.changedDeclines);
    expectSuccess(management.removePaymentReference(referenceId), 'remove reference');
    steps.awaitDecision('reference removed', next, expected.referenceRemoved);
    steps.bindTemplate(templateId, scope);
    steps.awaitDecision('reference restored', next, expected.referenceRestored);
    expectSuccess(management.removePaymentTemplate(templateId), 'remove template');
    steps.awaitDecision('template removed', next, expected.templateRemoved);
  },
};
