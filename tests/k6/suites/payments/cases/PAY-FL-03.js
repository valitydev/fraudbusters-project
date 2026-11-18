import data from '../../../data/payments/PAY-FL-03.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-03',
  title: 'Нет привязки или ни одно правило не сработало',
  profiles: ['functional', 'smoke'],
  run(context) {
    const steps = paymentSteps(context);
    steps.expectDecision(steps.payment(steps.scope('unbound')), data.expected.withoutReferences,
      'party without references');
    const scope = steps.scope();
    steps.applyTemplate('template', data.template, scope, data.ready);
    steps.expectDecision(steps.payment(scope), data.expected.noRuleMatched, 'no rule matched');
  },
};
