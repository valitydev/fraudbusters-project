import data from '../../../data/payments/PAY-FL-05.js';
import { expectFatalOnceReached } from '../../../lib/assert.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-05',
  title: 'sum по fingerprint с условием на валюту',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const base = { ...steps.scope(), fingerprint: context.ids.name('fingerprint') };
    steps.applyTemplate('template', data.template, base);
    const decisions = steps.decisionsAfterEach(base, data.loads);
    context.observe('decisions_after_each_10000', decisions.join(','));
    expectFatalOnceReached(decisions, 'sum');
    steps.expectDecision(steps.payment({ ...base, ...data.otherCurrency }), data.expected.otherCurrency,
      'currency condition');
  },
};
