import data from '../../../data/payouts/PO-API-09.js';
import { expectEqual, expectOutcomes, expectSuccess } from '../../../lib/assert.js';
import { byContinuation, expectPaginated } from '../../../lib/pagination.js';
import { waitFor } from '../../../lib/poll.js';
import { historyTime, historyWindow } from '../../../lib/time.js';
import { payoutSteps } from '../steps.js';

const filterOutcome = (name, found, expected) => ({
  name,
  actual: JSON.stringify(found) === JSON.stringify(expected) ? `${expected.length} own` : `${found.length} found`,
  expected: `${expected.length} own`,
});

export default {
  id: 'PO-API-09',
  title: 'История выплат: фильтры, последнее состояние, пагинация',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const { ids, payouts, connection } = context;
    const partyId = ids.name('party');
    const items = Array.from({ length: data.payouts }, (unused, index) => {
      const fields = {
        walletId: ids.name(`wallet-${index % 2}`),
        email: `${ids.prefix}-${index % 2}@k6.test`,
        cardToken: ids.name(`card-${index % 3}`),
        fingerprint: ids.name(`fingerprint-${index % 2}`),
        terminal: ids.name(`terminal-${index % 2}`),
        providerCountry: data.providerCountries[index % 2],
        userId: ids.name(`user-${index % 2}`),
        customerId: ids.name(`customer-${index % 2}`),
        status: index < data.succeeded ? 'succeeded' : 'failed',
      };
      const payout = steps.payout({ partyId, ...fields, terminalId: fields.terminal });
      return { payout, fields: { ...fields, payoutId: payout.id } };
    });
    steps.load(items.filter((item) => item.fields.status === 'succeeded').map((item) => item.payout), 'succeeded');
    steps.load(items.filter((item) => item.fields.status === 'failed').map((item) => item.payout), 'failed',
      data.failedErrorCode);
    const ownIds = items.map((item) => item.payout.id);
    const idsFound = (query) => [...new Set(steps.findPayoutsHistory({ partyId, ...query })
      .map((item) => item.id))].filter((id) => ownIds.includes(id)).sort();
    waitFor(connection, `${data.payouts} payouts in history`, () => {
      const found = idsFound({}).length;
      return { done: found === data.payouts, observed: found };
    });
    const probe = items[5].fields;
    const future = new Date(Date.now() + 3600 * 1000);
    expectOutcomes(context, [
      ...data.filters.map((name) => filterOutcome(`filter_${name}`, idsFound({ [name]: probe[name] }),
        items.filter((item) => item.fields[name] === probe[name]).map((item) => item.payout.id).sort())),
      filterOutcome('time_window', idsFound({
        from: historyTime(future), to: historyTime(new Date(future.getTime() + 3600 * 1000)),
      }), []),
    ]);

    const changing = steps.payout({ partyId, walletId: ids.name('wallet-0') });
    data.changing.forEach((status) => steps.loadAndAwait([changing], status));
    expectEqual(steps.findPayoutsHistory({ partyId, payoutId: changing.id }).map((item) => item.status),
      [data.changing[data.changing.length - 1]], 'payout is returned once in its last state');
    expectEqual(steps.findPayoutsHistory({ partyId, payoutId: changing.id, status: data.changing[0] }).length, 0,
      'status filter applies to the last state');

    const pages = expectPaginated((query) => expectSuccess(payouts.filterPayoutsHistory(
      { ...historyWindow(), partyId, ...query }), 'payout history page'), {
      size: data.pageSize, cursorAfter: byContinuation, expectedKeys: [...ownIds, changing.id], keyOf: (item) => item.id,
      what: 'payout history',
    });
    context.observe('history_pages', pages.map((page) => page.items.length).join(','));
  },
};
