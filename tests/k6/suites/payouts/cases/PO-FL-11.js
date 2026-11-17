import data from '../../../data/payouts/PO-FL-11.js';
import { expectSuccess } from '../../../lib/assert.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-11',
  title: 'Изменение и удаление шаблона выплаты',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const { payouts } = context;
    const { expected } = data;
    const scope = steps.scope('a');
    const next = () => steps.withdrawal(scope);
    const templateId = steps.applyTemplate('template', data.templates.accepts, scope, { decision: expected.accepts });
    steps.saveTemplate(templateId, data.templates.declines);
    steps.awaitDecision('changed template declines', next, expected.changedDeclines);
    expectSuccess(payouts.removePayoutReference(scope), 'remove reference');
    steps.awaitDecision('reference removed', next, expected.referenceRemoved);
    steps.bindTemplate(templateId, scope);
    steps.awaitDecision('reference restored', next, expected.referenceRestored);
    expectSuccess(payouts.removePayoutTemplate(templateId), 'remove template');
    steps.awaitDecision('template removed', next, expected.templateRemoved);
  },
};
