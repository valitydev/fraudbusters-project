import data from '../../../data/payments/PAY-FL-11.js';
import { expectStatus } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-11',
  title: 'Идентичность клиента: count по user_id в рамках party',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const [first, second] = steps.applyTemplates(['a', 'b'].map((role) => ({
      role, template: data.template, scope: { ...steps.scope(role), ...data.client },
    })));
    steps.loadEach(first.scope, data.loadsInFirstParty);
    steps.awaitDecision('user over the limit in the first party', () => steps.payment(first.scope),
      data.expected.firstParty);
    steps.expectDecision(steps.payment(second.scope), data.expected.secondParty, 'same user in the second party');
    expectStatus(context.inspection.inspectPayment(steps.payment({ ...second.scope, customerId: undefined }), ANY_STATUS),
      [data.expected.withoutCustomer], 'user without customer');
  },
};
