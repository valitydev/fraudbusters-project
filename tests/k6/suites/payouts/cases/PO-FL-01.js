import data from '../../../data/payouts/PO-FL-01.js';
import { expectEqual } from '../../../lib/assert.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-01',
  title: 'Выплата без риска: проверка, история',
  profiles: ['functional', 'smoke'],
  run(context) {
    const steps = payoutSteps(context);
    const scope = steps.scope('a');
    steps.applyTemplate('template', data.template, scope);
    const payout = steps.payout(scope);
    steps.expectDecision(payout, data.expected.decision, 'payout below the limit');
    const [stored] = steps.loadAndAwait([payout]);
    expectEqual([stored.partyId, stored.walletId], [scope.partyId, scope.walletId], 'scope in history');
    expectEqual({ amount: stored.amount, currency: stored.currency, status: stored.status }, data.expected.history,
      'payout in history');
    steps.expectDecision(steps.payout({ ...scope, ...data.declined }), data.expected.declined,
      'payout above the limit');
  },
};
