import data from '../../../data/mixed/MIX-02.js';
import { paymentSteps } from '../../payments/steps.js';
import { payoutSteps } from '../../payouts/steps.js';

export default {
  id: 'MIX-02',
  title: 'Платежи влияют на решение по выплате',
  profiles: ['functional'],
  run(context) {
    const payments = paymentSteps(context);
    const payouts = payoutSteps(context);
    const { ids } = context;
    const target = { partyId: ids.name('party'), walletId: ids.name('wallet'), ...data.client };
    payouts.bindNewTemplate('payout', data.payoutTemplate, { partyId: target.partyId });
    payouts.awaitReady('user without payments', target, { decision: data.expected.withoutPayments });
    payments.loadAndAwait([payments.payment({ partyId: target.partyId, shopId: ids.name('shop'), ...data.client })]);
    payouts.awaitDecision('user with a captured payment', () => payouts.withdrawal(target),
      data.expected.afterCapturedPayment);
  },
};
