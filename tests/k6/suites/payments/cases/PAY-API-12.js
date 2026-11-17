import data from '../../../data/payments/PAY-API-12.js';
import { expectOutcomes, expectSuccess } from '../../../lib/assert.js';
import { byContinuation, expectPaginated } from '../../../lib/pagination.js';
import { waitFor } from '../../../lib/poll.js';
import { historyTime, historyWindow } from '../../../lib/time.js';
import { paymentSteps } from '../steps.js';

const ownId = (item, ownIds) => [item.paymentId, item.id].find((value) => ownIds.has(value));

const filterOutcome = (name, found, expected) => ({
  name,
  actual: JSON.stringify(found) === JSON.stringify(expected) ? `${expected.length} own` : `${found.length} found`,
  expected: `${expected.length} own`,
});

export default {
  id: 'PAY-API-12',
  title: 'История платежей: фильтры и пагинация',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { ids, management, connection } = context;
    const partyId = ids.name('party');
    const items = Array.from({ length: data.payments }, (unused, index) => {
      const fields = {
        shopId: ids.name(`shop-${index % 2}`),
        email: `${ids.prefix}-${index % 2}@k6.test`,
        cardToken: ids.name(`card-${index % 3}`),
        fingerprint: ids.name(`fingerprint-${index % 2}`),
        terminal: ids.name(`terminal-${index % 2}`),
        providerCountry: data.providerCountries[index % 2],
        userId: ids.name(`user-${index % 2}`),
        customerId: ids.name(`customer-${index % 2}`),
        status: index < data.captured ? 'captured' : 'failed',
      };
      const payment = steps.payment({ partyId, ...fields, terminalId: fields.terminal });
      return { payment, fields: { ...fields, paymentId: payment.id } };
    });
    ['captured', 'failed'].forEach((status) => steps.load(
      items.filter((item) => item.fields.status === status).map((item) => item.payment), status));
    const ownIds = new Set(items.map((item) => item.payment.id));
    const idsFound = (query) => [...new Set(steps.findPaymentsHistory({ partyId, ...query })
      .map((item) => ownId(item, ownIds)))].sort();
    waitFor(connection, `${data.payments} payments in history`, () => {
      const found = idsFound({}).length;
      return { done: found === data.payments, observed: found };
    });
    const probe = items[5].fields;
    const future = new Date(Date.now() + 3600 * 1000);
    expectOutcomes(context, [
      ...data.filters.map((name) => filterOutcome(`filter_${name}`, idsFound({ [name]: probe[name] }),
        items.filter((item) => item.fields[name] === probe[name]).map((item) => item.payment.id).sort())),
      filterOutcome('time_window', idsFound({
        from: historyTime(future), to: historyTime(new Date(future.getTime() + 3600 * 1000)),
      }), []),
    ]);
    const pages = expectPaginated((query) => expectSuccess(management.filterPaymentsHistory(
      { ...historyWindow(), partyId, ...query }), 'history page'), {
      size: data.pageSize, cursorAfter: byContinuation, expectedKeys: [...ownIds], keyOf: (item) => ownId(item, ownIds),
      what: 'payments history',
    });
    context.observe('history_pages', pages.map((page) => page.items.length).join(','));
  },
};
