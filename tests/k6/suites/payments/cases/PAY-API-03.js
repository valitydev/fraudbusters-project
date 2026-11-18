import data from '../../../data/payments/PAY-API-03.js';
import { expectEqual, expectSuccess } from '../../../lib/assert.js';
import { byLastId, expectPaginated } from '../../../lib/pagination.js';
import { waitForCount } from '../../../lib/poll.js';

export default {
  id: 'PAY-API-03',
  title: 'Поиск шаблонов и пагинация',
  profiles: ['functional'],
  run(context) {
    const { management, ids, connection } = context;
    const templates = ids.numbered('t', data.templates);
    templates.forEach((id) => expectSuccess(management.savePaymentTemplate({ id, template: data.template }),
      `save ${id}`));
    const search = (query) => expectSuccess(management.filterPaymentTemplates({ searchValue: ids.name('t'), ...query }),
      'filter templates');
    waitForCount(connection, 'templates of the case', () => search({ size: 100 }), data.templates);
    data.orders.forEach((sortOrder) => expectPaginated((query) => search({ sortOrder, ...query }), {
      size: data.pageSize, cursorAfter: byLastId, expectedKeys: templates, keyOf: (item) => item.id, order: sortOrder,
      expectedCount: data.templates, what: `templates ${sortOrder}`,
    }));
    const idsInOrder = (query) => search({ size: data.templates, ...query }).result.map((item) => item.id);
    const ascending = idsInOrder({ sortOrder: 'ASC' });
    expectEqual(idsInOrder({ sortOrder: 'DEFAULT' }), ascending, 'DEFAULT order equals ASC');
    expectEqual(idsInOrder({}), ascending, 'missing sortOrder equals ASC');
    expectEqual(expectSuccess(management.filterPaymentTemplates({ searchValue: templates[3], size: data.templates }),
      'filter templates').result.map((item) => item.id), [templates[3]], 'search by a full id');
  },
};
