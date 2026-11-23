import data from '../../../data/payouts/PO-FL-07.js';
import { expectEqual, expectFatalOnceReached } from '../../../lib/assert.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-07',
  title: 'countPayoutsSuccess, sumPayoutsSuccess',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const [counted, summed] = ['count', 'sum'].map((role) => ({
      ...steps.scope(role), cardToken: context.ids.name(`card-${role}`),
    }));
    steps.applyTemplates([
      { role: 'count', template: data.count.template, scope: counted },
      { role: 'sum', template: data.sum.template, scope: summed },
    ]);
    const counts = steps.decisionsAfterEach(counted, data.count.loads);
    context.observe('decisions_3_failed_then_3_succeeded', counts.join(','));
    expectEqual(counts.slice(0, 3), data.count.expected.afterFailed, 'failed payouts are not counted');
    expectFatalOnceReached(counts, 'countPayoutsSuccess');
    const sums = steps.decisionsAfterEach(summed, data.sum.loads);
    context.observe('sum_decisions_failed30000_succeeded10000_succeeded10000', sums.join(','));
    expectEqual(sums[0], data.sum.expected.afterFailed, 'failed payout is not summed');
    expectEqual(sums[2], data.sum.expected.afterTwoSucceeded, 'succeeded payouts are summed');
  },
};
