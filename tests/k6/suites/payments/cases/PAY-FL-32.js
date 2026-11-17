import data from '../../../data/payments/PAY-FL-32.js';
import { expectEqual } from '../../../lib/assert.js';
import { waitFor } from '../../../lib/poll.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-32',
  title: 'Thrift IsBlacklisted',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { ids, payoutInspector } = context;
    const scope = steps.scope();
    const card = ids.name('black-card');
    steps.addListRow(data.list.type, data.list.name, card, scope);
    steps.awaitListRow(data.list.type, data.list.name, card);
    const isBlacklisted = (overrides) => payoutInspector.isBlacklisted({
      ...scope, fieldName: data.list.name, value: card, ...overrides,
    }).blacklisted;
    waitFor(context.connection, 'card is blacklisted', () => {
      const answer = isBlacklisted({});
      return { done: answer === true, observed: answer };
    });
    expectEqual({
      listedCard: isBlacklisted({}),
      otherCard: isBlacklisted({ value: ids.name('other-card') }),
      sameCardInOtherShop: isBlacklisted({ shopId: ids.name('other-shop') }),
    }, data.expected, 'IsBlacklisted answers');
  },
};
