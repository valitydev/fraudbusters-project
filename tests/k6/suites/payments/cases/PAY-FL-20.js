import data from '../../../data/payments/PAY-FL-20.js';
import { expectEqual } from '../../../lib/assert.js';
import { waitFor } from '../../../lib/poll.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-20',
  title: 'Каскад: группа и шаблон, приоритеты в группе',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { ids } = context;
    const { expected } = data;
    const shopId = ids.name('shop');
    const accept = steps.saveTemplate(ids.name('accept'), data.templates.accept);
    const decline = steps.saveTemplate(ids.name('decline'), data.templates.decline);
    const noDecision = steps.saveTemplate(ids.name('no-decision'), data.templates.noDecision);
    const group = (role, priorities) => steps.saveGroup(ids.name(`group-${role}`), priorities);
    const at = (partyId) => () => steps.payment({ partyId, shopId });

    const partyA = ids.name('party-a');
    steps.bindGroup(group('a', [[decline, 1]]), { partyId: partyA });
    steps.bindTemplate(accept, { partyId: partyA });
    steps.awaitDecision('party group before party template', at(partyA), expected.partyGroupBeforePartyTemplate);

    const partyB = ids.name('party-b');
    steps.bindGroup(group('b', [[accept, 1]]), { partyId: partyB, shopId });
    steps.bindTemplate(decline, { partyId: partyB });
    steps.awaitDecision('party+shop group before party template', at(partyB), expected.shopGroupBeforePartyTemplate);

    const partyC = ids.name('party-c');
    steps.bindGroup(group('c', [[accept, 1], [decline, 2]]), { partyId: partyC });
    const decisionOtherThan = (description, excluded) => waitFor(context.connection, description, () => {
      const decision = steps.inspect(at(partyC)());
      return { done: !excluded.includes(decision), value: decision, observed: decision };
    });
    const first = decisionOtherThan('group with priorities is applied', ['high']);
    group('c', [[accept, 2], [decline, 1]]);
    const second = decisionOtherThan('swapped priorities change the decision', ['high', first]);
    context.observe('decision_accept_priority_1', first);
    context.observe('decision_accept_priority_2', second);
    expectEqual([first, second].sort(), expected.bothPriorityOrders, 'decisions of both priority orders');

    const partyD = ids.name('party-d');
    steps.bindGroup(group('d', [[noDecision, 1], [decline, 2]]), { partyId: partyD });
    steps.awaitDecision('template without decision falls through inside the group', at(partyD),
      expected.noDecisionFallsThroughInGroup);
  },
};
