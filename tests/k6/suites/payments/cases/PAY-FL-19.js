import data from '../../../data/payments/PAY-FL-19.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-19',
  title: 'Каскад: шаблон party и party+shop',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { ids } = context;
    const partyId = ids.name('party');
    const shop = { partyId, shopId: ids.name('shop-1') };
    const otherShop = { partyId, shopId: ids.name('shop-2') };
    const { expected } = data;
    steps.applyTemplate('template-shop', data.shopTemplate, shop, { decision: expected.onlyShopTemplate });
    const partyTemplate = steps.bindNewTemplate('template-party', data.partyTemplates.declines, { partyId });
    steps.awaitDecision('party template comes first', () => steps.payment(shop), expected.partyTemplateFirst);
    steps.saveTemplate(partyTemplate, data.partyTemplates.noDecision);
    steps.awaitDecision('party template without decision falls through', () => steps.payment(shop),
      expected.partyWithoutDecisionFallsThrough);
    steps.saveTemplate(partyTemplate, data.partyTemplates.declines);
    steps.awaitDecision('party template for another shop', () => steps.payment(otherShop),
      expected.partyTemplateForOtherShop);
  },
};
