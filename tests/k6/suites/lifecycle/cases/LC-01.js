import data from '../../../data/lifecycle/LC-01.js';
import { paymentSteps } from '../../payments/steps.js';

export default {
  id: 'LC-01',
  title: 'Шаблон и привязка платежа после перезапуска',
  prepare(context) {
    const steps = paymentSteps(context);
    steps.applyTemplate('template', data.template, steps.scope());
  },
  verify(context) {
    const steps = paymentSteps(context);
    const scope = steps.scope();
    steps.awaitDecision('template restored', () => steps.payment({ ...scope, ...data.declined }), data.expected.declined);
    steps.expectDecision(steps.payment(scope), data.expected.accepted, 'restored template accepts small payments');
  },
};
