import data from '../../../data/payments/PAY-API-09.js';
import { expectEqual, expectSuccess, expectTrue } from '../../../lib/assert.js';
import { expectPaginated } from '../../../lib/pagination.js';
import { waitForCount } from '../../../lib/poll.js';
import { countInfoFor } from '../../../lib/time.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-API-09',
  title: 'Списки: строки, имена, поиск',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { management, ids, connection } = context;
    const { white, grey, naming, black } = ids.resolve(data);
    const scope = steps.scope();
    const available = expectSuccess(management.fetchAvailablePaymentListNames(), 'available list names');
    context.observe('available_list_names', JSON.stringify(available.result || available));
    steps.addListRow('white', white.name, white.value, scope);
    const { count, fromMs, toMs } = grey.countInfo;
    steps.addListRow('grey', grey.name, grey.value, scope, countInfoFor(count, fromMs, toMs));
    const namingRow = management.savePaymentListRows('naming', [{ listRecord: { listName: naming.name, value: naming.value,
      ...scope } }]);
    context.observe('naming_row_status', namingRow.status);
    const blackParty = { partyId: ids.name('black-party'), shopId: scope.shopId };
    const values = ids.numbered('black', black.rows);
    values.forEach((value) => steps.addListRow('black', black.name, value, blackParty));

    const whiteRow = steps.awaitListRow('white', white.name, white.value);
    expectEqual([whiteRow.partyId, whiteRow.shopId, whiteRow.listType], [scope.partyId, scope.shopId, 'white'],
      'white row fields');
    const greyRow = steps.awaitListRow('grey', grey.name, grey.value);
    expectTrue(String(greyRow.rowInfo).includes(String(count)), `grey row keeps its count: ${greyRow.rowInfo}`);
    const current = expectSuccess(management.fetchCurrentPaymentListNames('white'), 'current white list names');
    expectTrue(JSON.stringify(current).includes(white.name), `current white list names: ${JSON.stringify(current)}`);

    const search = (query) => expectSuccess(management.filterPaymentLists({
      listType: 'black', listNames: [black.name], searchValue: blackParty.partyId, ...query,
    }), 'filter black list');
    waitForCount(connection, 'black rows', () => search({ size: 100 }), black.rows);
    expectPaginated(search, {
      size: black.pageSize, cursorAfter: (last) => ({ lastId: last.id, sortFieldValue: last.insertTime }),
      expectedKeys: values, keyOf: (item) => item.value, expectedCount: black.rows, what: 'black list rows',
    });
    const row = search({ size: 100 }).result.find((item) => item.value === values[0]);
    expectSuccess(management.removePaymentListRow(row.id), 'remove list row');
    waitForCount(connection, 'black rows after removal', () => search({ size: 100 }), black.rows - 1);
  },
};
