import data from '../../../data/lifecycle/LC-04.js';
import { paymentSteps } from '../../payments/steps.js';

export default {
  id: 'LC-04',
  title: 'Агрегаты по истории платежей после перезапуска',
  prepare(context) {
    const steps = paymentSteps(context);
    const card = { ...steps.scope(), cardToken: context.ids.name('card') };
    steps.applyTemplate('template', data.template, card);
    steps.loadAndAwait(data.loads.map(() => steps.payment(card)));
    steps.awaitDecision('card over the limit', () => steps.payment(card), data.expected);
  },
  verify(context) {
    const steps = paymentSteps(context);
    steps.awaitDecision('history restored', () => steps.payment({ ...steps.scope(), cardToken: context.ids.name('card') }),
      data.expected);
  },
};
