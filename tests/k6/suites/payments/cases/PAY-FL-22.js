import data from '../../../data/payments/PAY-FL-22.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-22',
  title: 'Каскад: notify не останавливает проверку',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { ids } = context;
    const partyId = ids.name('party');
    steps.bindNewTemplate('notify', data.partyTemplate, { partyId });
    steps.applyTemplate('decline', data.shopTemplate, { partyId, shopId: ids.name('shop-1') },
      { decision: data.expected.shopDecisionAfterPartyNotify });
    steps.expectDecision(steps.payment({ partyId, shopId: ids.name('shop-2') }), data.expected.onlyNotify,
      'only notify, no decision');
  },
};
