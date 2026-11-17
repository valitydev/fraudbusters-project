import data from '../../../data/payments/PAY-FL-31.js';
import { expectOutcomes } from '../../../lib/assert.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-31',
  title: 'Агрегаты по полям: email, ip, fingerprint, phone, bin, country_bank',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const variants = steps.applyTemplates(data.variants.map((variant) => ({
      ...variant,
      scope: steps.scope(variant.role),
      ready: variant.payment,
    })));
    steps.loadAndAwait(variants.flatMap((variant) => data.loads.map(() => steps.payment({
      ...variant.scope, ...variant.payment,
    }))));
    expectOutcomes(context, variants.map((variant) => ({
      name: variant.role, actual: steps.inspect(steps.payment({ ...variant.scope, ...variant.payment })),
      expected: variant.expected,
    })));
  },
};
