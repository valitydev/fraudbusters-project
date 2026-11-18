import data from '../../../data/payments/PAY-FL-16.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-16',
  title: 'Доверенная карта isTrusted',
  profiles: ['functional'],
  until: 'payouts',
  run(context) {
    const steps = paymentSteps(context);
    const scope = steps.scope();
    const card = { ...scope, cardToken: context.ids.name('card') };
    steps.applyTemplate('template', data.template, scope, data.ready);
    steps.expectDecision(steps.payment(card), data.expected.withoutHistory, 'card without history');
    steps.loadEach(card, data.loads);
    steps.awaitDecision('card becomes trusted', () => steps.payment(card), data.expected.trusted);
  },
};
