import data from '../../../data/payouts/PO-FL-02.js';
import { expectTrue } from '../../../lib/assert.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-02',
  title: 'Нет привязки и неполный контекст',
  profiles: ['functional', 'smoke'],
  run(context) {
    const steps = payoutSteps(context);
    const scope = steps.scope('unbound');
    steps.expectDecision(steps.withdrawal(scope), data.expected.withoutReferences, 'withdrawal without references');
    Object.entries(data.incomplete).forEach(([name, overrides]) => {
      const answer = context.payoutInspector.inspectWithdrawal(steps.withdrawal({ ...scope, ...overrides }));
      context.observe(name, JSON.stringify(answer.invalidRequest || answer));
      expectTrue(Array.isArray(answer.invalidRequest) && answer.invalidRequest.length > 0,
        `${name} must be InvalidRequest: ${JSON.stringify(answer)}`);
    });
  },
};
