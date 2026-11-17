import data from '../../../data/payments/PAY-FL-28.js';
import { expectOutcomes, expectStatus, expectSuccess, expectTrue, validationErrors } from '../../../lib/assert.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-28',
  title: 'Результаты правил: 3ds, *AndNotify, уведомления, catch',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { ids, management, inspection } = context;
    const partyId = ids.name('party');
    const variants = data.variants.map((variant) => ({
      ...variant, scope: { partyId, shopId: ids.name(`shop-${variant.role}`) },
    }));
    variants.forEach((variant) => {
      variant.templateId = steps.bindNewTemplate(`template-${variant.role}`, variant.template, variant.scope);
    });
    const outcomes = variants.map((variant) => {
      const payment = steps.awaitReady(`${variant.role} is applied`, variant.scope, { decision: variant.expected.rest });
      const stored = steps.awaitInspectResult(payment);
      const decision = `${String(stored.resultStatus).toLowerCase()}/${stored.ruleChecked || ''}`;
      context.observe(`${variant.role}_notifications`, (stored.notificationsRule || []).join('+'));
      return { name: variant.role, actual: decision, expected: variant.expected.stored };
    });
    const invalidCatch = validationErrors(expectSuccess(management.validatePaymentTemplate(
      { id: ids.name('catch-decline'), template: data.invalidCatch }), 'validate catch: decline'));
    context.observe('catch_decline_is_invalid', invalidCatch.length > 0);
    expectTrue(invalidCatch.length > 0, 'catch: decline is not allowed by the grammar');
    expectStatus(inspection.inspectPayment(steps.payment(variants[0].scope)), [200], '3ds answer');
    expectOutcomes(context, outcomes);
  },
};
