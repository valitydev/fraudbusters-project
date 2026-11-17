import data from '../../../data/payouts/PO-FL-13.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-13',
  title: 'Группировка истории выплат по wallet_id',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const { ids } = context;
    const partyId = ids.name('party');
    const wallet = (role) => ({ partyId, walletId: ids.name(`wallet-${role}`), cardToken: ids.name('card') });
    steps.bindNewTemplate('template', data.template, { partyId });
    steps.awaitReady('template is applied', wallet('a'));
    steps.loadEach(wallet('a'), data.loadsInFirstWallet);
    steps.expectDecision(steps.withdrawal(wallet('a')), data.expected.firstWallet, 'card over the limit in wallet A');
    steps.expectDecision(steps.withdrawal(wallet('b')), data.expected.secondWallet,
      'same card in wallet B of the same party');
  },
};
