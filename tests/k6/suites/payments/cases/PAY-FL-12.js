import data from '../../../data/payments/PAY-FL-12.js';
import { expectSuccess } from '../../../lib/assert.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-12',
  title: 'Чёрный список',
  profiles: ['functional', 'smoke'],
  run(context) {
    const steps = paymentSteps(context);
    const scope = steps.scope();
    const card = { ...scope, cardToken: context.ids.name('card') };
    steps.applyTemplate('template', data.template, card);
    steps.addListRow(data.list.type, data.list.name, card.cardToken, scope);
    steps.awaitDecision('card in the list', () => steps.payment(card), data.expected.listed);
    const row = steps.awaitListRow(data.list.type, data.list.name, card.cardToken);
    expectSuccess(context.management.removePaymentListRow(row.id), 'remove list row');
    steps.awaitDecision('card removed from the list', () => steps.payment(card), data.expected.removed);
  },
};
