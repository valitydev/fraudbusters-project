import data from '../../../data/payouts/PO-FL-08.js';
import { expectEqual, expectFatalOnceReached } from '../../../lib/assert.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-08',
  title: 'countPayoutsError, sumPayoutsError с кодом ошибки',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const variants = steps.applyTemplates(data.variants.map((variant) => ({
      ...variant, scope: { ...steps.scope(variant.role), cardToken: context.ids.name(`card-${variant.role}`) },
    })));
    const results = Object.fromEntries(variants.map((variant) => {
      const decisions = steps.decisionsAfterEach(variant.scope, data.loads);
      context.observe(`${variant.role}_other_other_limit_limit_succeeded`, decisions.join(','));
      return [variant.role, decisions];
    }));
    expectEqual(results.code.slice(0, 2), data.expected.otherCodeIsNotCounted, 'errors with another code are not counted');
    expectFatalOnceReached(results.code, 'countPayoutsError with code');
    expectEqual(results.any[1], data.expected.anyCodeAfterTwoErrors, 'without a code every error is counted');
    expectEqual(results['sum-code'].slice(0, 2), data.expected.otherCodeIsNotCounted, 'sum of errors with another code');
    expectFatalOnceReached(results['sum-code'], 'sumPayoutsError with code');
  },
};
