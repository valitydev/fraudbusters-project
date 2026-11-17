import data from '../../../data/payments/PAY-FL-11.js';
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
    const withoutCustomer = context.inspection.inspectPayment(steps.payment({ ...second.scope, customerId: undefined }));
    context.observe('inspect_user_without_customer_status', withoutCustomer.status);
  },
};
