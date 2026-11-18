import data from '../../../data/payouts/PO-API-03.js';
import { expectSuccess } from '../../../lib/assert.js';
import { byLastId, expectPaginated } from '../../../lib/pagination.js';
import { waitForCount } from '../../../lib/poll.js';

export default {
  id: 'PO-API-03',
  title: 'Поиск шаблонов выплат и пагинация',
  profiles: ['functional'],
  run(context) {
    const { payouts, management, ids, connection } = context;
    const prefix = ids.name('t');
    const templates = ids.numbered('t', data.templates);
    templates.forEach((id) => expectSuccess(payouts.savePayoutTemplate({ id, template: data.template }), `save ${id}`));
    expectSuccess(management.savePaymentTemplate({ id: `${prefix}-payment`, template: data.template }),
      'save payment template with the same prefix');
    const search = (query) => expectSuccess(payouts.filterPayoutTemplates({ searchValue: prefix, ...query }),
      'filter payout templates');
    waitForCount(connection, 'payout templates of the case', () => search({ size: 100 }), data.templates);
    data.orders.forEach((sortOrder) => expectPaginated((query) => search({ sortOrder, ...query }), {
      size: data.pageSize, cursorAfter: byLastId, expectedKeys: templates, keyOf: (item) => item.id, order: sortOrder,
      expectedCount: data.templates, what: `payout templates ${sortOrder}`,
    }));
  },
};
