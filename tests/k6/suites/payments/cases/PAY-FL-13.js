import data from '../../../data/payments/PAY-FL-13.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-13',
  title: 'Белый список',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const scope = steps.scope();
    const listed = { ...scope, email: context.ids.resolve(data.email) };
    steps.applyTemplate('template', data.template, scope, data.ready);
    steps.expectDecision(steps.payment(listed), data.expected.notListed, 'email not in the list');
    steps.addListRow(data.list.type, data.list.name, listed.email, scope);
    steps.awaitDecision('email in the list', () => steps.payment(listed), data.expected.listed);
  },
};
