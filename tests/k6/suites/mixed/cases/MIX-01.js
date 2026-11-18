import data from '../../../data/mixed/MIX-01.js';
import { paymentSteps } from '../../payments/steps.js';
import { payoutSteps } from '../../payouts/steps.js';

export default {
  id: 'MIX-01',
  title: 'Успешные выплаты влияют на решение по платежу',
  profiles: ['functional', 'smoke'],
  run(context) {
    const payments = paymentSteps(context);
    const payouts = payoutSteps(context);
    const { ids } = context;
    const shopId = ids.name('shop');
    const party = { partyId: ids.name('party'), shopId, ...data.client };
    const otherParty = { partyId: ids.name('party-other'), shopId, ...data.client };
    const templateId = payments.bindNewTemplate('payment', data.paymentTemplate, party);
    payments.bindTemplate(templateId, otherParty);
    [party, otherParty].forEach((scope) => payments.awaitReady('payment template is applied', scope));
    payouts.loadEach({ partyId: party.partyId, walletId: ids.name('wallet'), ...data.client }, data.succeededPayouts);
    payments.awaitDecision('user with succeeded payouts', () => payments.payment(party), data.expected.afterPayouts);
    payments.expectDecision(payments.payment(otherParty), data.expected.otherParty,
      'payouts of another party are not counted');
  },
};
