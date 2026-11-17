import data from '../../../data/payments/PAY-FL-06.js';
import { expectFatalOnceReached } from '../../../lib/assert.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-06',
  title: 'unique email по ip',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const base = { ...steps.scope(), ip: data.ip };
    const withEmail = (index) => ({ ...base, email: `${context.ids.prefix}-${index}@k6.test` });
    steps.applyTemplate('template', data.template, withEmail(0));
    const decisions = [];
    for (let index = 1; index <= data.newEmails; index += 1) {
      steps.loadAndAwait([steps.payment(withEmail(index))]);
      decisions.push(steps.inspect(steps.payment(withEmail(index + 10))));
    }
    context.observe('decisions_after_each_new_email', decisions.join(','));
    expectFatalOnceReached(decisions, 'unique');
  },
};
