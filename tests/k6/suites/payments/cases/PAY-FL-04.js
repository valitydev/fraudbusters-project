import data from '../../../data/payments/PAY-FL-04.js';
import { expectFatalOnceReached } from '../../../lib/assert.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-04',
  title: 'count по card_token в рамках party и shop',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const card = { partyId: context.ids.name('party'), cardToken: context.ids.name('card') };
    const ownShop = { ...card, shopId: context.ids.name('shop-a') };
    const otherShop = { ...card, shopId: context.ids.name('shop-b') };
    steps.applyTemplate('template', data.template, ownShop);
    steps.loadEach(otherShop, data.loadsInOtherShop);
    steps.expectDecision(steps.payment(ownShop), data.expected.otherShop, 'payments of another shop are not counted');
    const decisions = steps.decisionsAfterEach(ownShop, data.loadsInOwnShop);
    context.observe('decisions_after_each_loaded_payment', decisions.join(','));
    expectFatalOnceReached(decisions, 'count');
  },
};
