import data from '../../../data/payouts/PO-API-12.js';
import { expectStatus } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';
import { payoutRecord } from '../../../fixtures/payout.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-API-12',
  title: 'Загрузка выплат в fraudbusters-api',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const { inspection, ids } = context;
    expectStatus(inspection.loadPayouts([], ANY_STATUS), [data.expected.empty], 'empty payouts');
    expectStatus(inspection.loadPayouts([ids.resolve(data.broken)], ANY_STATUS), [data.expected.broken],
      'payout without required fields');
    const walletOnly = payoutRecord(steps.payout(data.walletWithoutParty), 'succeeded');
    expectStatus(inspection.loadPayouts([walletOnly], ANY_STATUS), [data.expected.walletWithoutParty],
      'wallet without party');
  },
};
