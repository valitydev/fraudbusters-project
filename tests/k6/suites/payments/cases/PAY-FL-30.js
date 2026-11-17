import data from '../../../data/payments/PAY-FL-30.js';
import { expectOutcomes } from '../../../lib/assert.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-30',
  title: 'Окна времени: minutes, calendar_days, calendar_months, смещение, без единицы',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const variants = steps.applyTemplates(data.variants.map((variant) => ({
      ...variant,
      scope: { ...steps.scope(variant.role), cardToken: context.ids.name(`card-${variant.role}`) },
    })));
    steps.loadAndAwait(variants.flatMap((variant) => data.loads.map(() => steps.payment(variant.scope))));
    expectOutcomes(context, variants.map((variant) => ({
      name: variant.role, actual: steps.inspect(steps.payment(variant.scope)), expected: variant.expected,
    })));
  },
};
