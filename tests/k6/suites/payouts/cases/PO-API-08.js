import data from '../../../data/payouts/PO-API-08.js';
import { expectEqual, expectOutcomes, expectSuccess, expectTrue, sameMembers } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';
import { expectPaginated } from '../../../lib/pagination.js';
import { waitForCount } from '../../../lib/poll.js';
import { countInfoFor } from '../../../lib/time.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-API-08',
  title: 'Списки выплат',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const { payouts, management, ids, connection } = context;
    const { white, grey, naming, invalidRows, black, expected } = ids.resolve(data);
    const scope = { partyId: ids.name('party'), walletId: ids.name('wallet') };
    const available = expectSuccess(payouts.fetchAvailablePayoutListNames(), 'available payout list names');
    sameMembers(available.result || available, data.availableListNames, 'available payout list names');

    steps.addListRow('white', white.name, white.value, scope);
    const { count, fromMs, toMs } = grey.countInfo;
    steps.addListRow('grey', grey.name, grey.value, { partyId: scope.partyId }, countInfoFor(count, fromMs, toMs));
    steps.addListRow('naming', naming.name, naming.value);
    expectOutcomes(context, Object.entries(invalidRows).map(([name, listRecord]) => ({
      name, actual: payouts.savePayoutListRows('black', [{ listRecord }], ANY_STATUS).status, expected: expected.invalidRow,
    })));

    const blackParty = ids.name('black-party');
    const values = ids.numbered('black', black.rows);
    values.forEach((value) => steps.addListRow('black', black.name, value, { partyId: blackParty }));
    expectSuccess(management.savePaymentListRows('black', [{
      listRecord: { listName: black.name, value: values[0], partyId: blackParty, shopId: ids.name('shop') },
    }]), 'payment row with the same value');

    expectEqual(steps.awaitListRow('white', white.name, white.value, scope).listType, 'white', 'white row type');
    const greyRow = steps.awaitListRow('grey', grey.name, grey.value, { partyId: scope.partyId });
    expectEqual(greyRow.countInfo && greyRow.countInfo.count, count, 'grey row keeps its count');
    steps.awaitListRow('naming', naming.name, naming.value, {});
    const current = expectSuccess(payouts.fetchCurrentPayoutListNames('white'), 'current white list names');
    expectTrue(JSON.stringify(current).includes(white.name), `current white list names: ${JSON.stringify(current)}`);

    const search = (query) => expectSuccess(payouts.filterPayoutLists({
      listType: 'black', listNames: [black.name], searchValue: blackParty, ...query,
    }), 'filter payout black list');
    waitForCount(connection, 'payout black rows', () => search({ size: 100 }), black.rows);
    expectPaginated((query) => search({ sortOrder: 'ASC', ...query }), {
      size: black.pageSize, cursorAfter: (last) => ({ lastId: last.id }), expectedKeys: values,
      keyOf: (item) => item.listRecord.value, sortKeyOf: (item) => item.id, order: 'ASC', expectedCount: black.rows,
      what: 'payout black rows',
    });
    const row = search({ size: 100 }).result.find((item) => item.listRecord.value === values[0]);
    expectSuccess(payouts.removePayoutListRow(row.id), 'remove payout row');
    waitForCount(connection, 'payout black rows after removal', () => search({ size: 100 }), black.rows - 1);
    expectEqual(payouts.removePayoutListRow(ids.name('missing'), ANY_STATUS).status, expected.removeMissing,
      'remove a missing row');
  },
};
