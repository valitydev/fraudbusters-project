import data from '../../../data/payments/PAY-FL-07.js';
import { expectOutcomes } from '../../../lib/assert.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-07',
  title: 'countSuccess, countError, countPending',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const variants = steps.applyTemplates(data.variants.map((variant) => ({
      ...variant,
      scope: { ...steps.scope(variant.role), cardToken: context.ids.name(`card-${variant.role}`) },
    })));
    variants.forEach((variant) => steps.loadEach(variant.scope, variant.loads));
    expectOutcomes(context, variants.map((variant) => ({
      name: variant.role, actual: steps.inspect(steps.payment(variant.scope)), expected: variant.expected,
    })));
  },
};
