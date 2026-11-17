import data from '../../../data/payments/PAY-FL-23.js';
import { expectEqual, expectSuccess } from '../../../lib/assert.js';
import { waitFor } from '../../../lib/poll.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-23',
  title: 'Привязка по умолчанию для нового магазина',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { ids, management } = context;
    const newShop = { partyId: ids.name('party'), shopId: ids.name('new-shop') };
    const templateId = steps.saveTemplate(ids.name('default'), data.defaultTemplate);
    expectSuccess(management.saveDefaultReference({ partyId: newShop.partyId, templateId }), 'save default reference');
    steps.expectDecision(steps.payment(newShop), data.expected.firstPaymentOfNewShop, 'first payment of a new shop');
    const created = waitFor(context.connection, 'reference created from the default one', () => {
      const found = steps.findReferences(templateId)
        .find((item) => item.partyId === newShop.partyId && item.shopId === newShop.shopId);
      return { done: Boolean(found), value: found };
    });
    expectEqual(created.templateId, templateId, 'template of the created reference');
    steps.awaitDecision('default template applied to the new shop', () => steps.payment(newShop),
      data.expected.afterReferenceCreated);
  },
};
