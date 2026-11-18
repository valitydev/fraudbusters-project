import data from '../../../data/payments/PAY-FL-14.js';
import { expectEqual } from '../../../lib/assert.js';
import { waitFor } from '../../../lib/poll.js';
import { countInfoFor } from '../../../lib/time.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-14',
  title: 'Серый список с лимитом',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const scope = steps.scope();
    const card = { ...scope, cardToken: context.ids.name('card') };
    const { count, fromMs, toMs } = data.countInfo;
    steps.applyTemplate('template', data.template, scope, data.ready);
    steps.expectDecision(steps.payment(card), data.expected.notListed, 'card not in the list');
    steps.addListRow(data.list.type, data.list.name, card.cardToken, scope, countInfoFor(count, fromMs, toMs));
    const payment = steps.awaitDecision('card in the list', () => steps.payment(card), data.expected.listed);
    steps.loadAndAwait([payment]);
    const after = waitFor(context.connection, 'grey list limit is exhausted', () => {
      const decision = steps.inspect(steps.payment(card));
      return { done: decision !== data.expected.listed, value: decision, observed: decision };
    });
    context.observe('decision_after_limit', after);
    expectEqual(after, data.expected.afterLimit, 'decision after the grey list limit');
  },
};
