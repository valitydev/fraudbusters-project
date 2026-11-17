import data from '../../../data/payouts/PO-API-12.js';
import { expectStatus } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';
import { withdrawalRecord } from '../../../fixtures/withdrawal.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-API-12',
  title: 'Загрузка выплат в fraudbusters-api',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const { inspection, ids } = context;
    expectStatus(inspection.loadWithdrawals([], ANY_STATUS), [data.expected.empty], 'empty withdrawals');
    expectStatus(inspection.loadWithdrawals([ids.resolve(data.broken)], ANY_STATUS), [data.expected.broken],
      'withdrawal without required fields');
    const walletOnly = withdrawalRecord(steps.withdrawal(data.walletWithoutParty), 'succeeded');
    expectStatus(inspection.loadWithdrawals([walletOnly], ANY_STATUS), [data.expected.walletWithoutParty],
      'wallet without party');
  },
};
