import data from '../../../data/payouts/PO-FL-09.js';
import { expectFatalOnceReached } from '../../../lib/assert.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-09',
  title: 'uniquePayouts',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const base = { ...steps.scope('unique'), ...data.client };
    steps.applyTemplate('unique', data.template, base);
    const decisions = steps.decisionsAfterEach(base, data.loads);
    context.observe('decisions_after_each_new_card', decisions.join(','));
    expectFatalOnceReached(decisions, 'uniquePayouts');
  },
};
