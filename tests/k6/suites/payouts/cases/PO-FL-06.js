import data from '../../../data/payouts/PO-FL-06.js';
import { expectEqual, expectFatalOnceReached } from '../../../lib/assert.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-06',
  title: 'countPayouts, sumPayouts',
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
    context.observe('count_decisions_succeeded_failed_pending_pending_succeeded_failed', counts.join(','));
    expectEqual(counts[3], counts[1], 'pending payouts are not counted');
    expectFatalOnceReached(counts, 'countPayouts');
    const sums = steps.decisionsAfterEach(summed, data.sum.loads);
    context.observe('sum_decisions_pending30000_succeeded10000_failed10000', sums.join(','));
    expectEqual(sums[0], data.sum.expected.afterPending, 'pending payout is not summed');
    expectEqual(sums[2], data.sum.expected.afterSucceededAndFailed, 'succeeded and failed payouts are summed');
  },
};
