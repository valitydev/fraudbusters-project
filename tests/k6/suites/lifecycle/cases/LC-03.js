import data from '../../../data/lifecycle/LC-03.js';
import { paymentSteps } from '../../payments/steps.js';

export default {
  id: 'LC-03',
  title: 'Чёрный список платежей после перезапуска',
  prepare(context) {
    const steps = paymentSteps(context);
    const scope = steps.scope();
    steps.bindNewTemplate('template', data.template, scope);
    steps.addListRow('black', 'CARD_TOKEN', context.ids.resolve(data.listedCard), scope);
    steps.awaitDecision('listed card', () => steps.payment({ ...scope, cardToken: data.listedCard }),
      data.expected.listedCard);
  },
  verify(context) {
    const steps = paymentSteps(context);
    const scope = steps.scope();
    steps.awaitDecision('listed card restored', () => steps.payment({ ...scope, cardToken: data.listedCard }),
      data.expected.listedCard);
    steps.expectDecision(steps.payment({ ...scope, cardToken: '{{other-card}}' }), data.expected.otherCard, 'other card');
  },
};
