import data from '../../../data/payments/PAY-API-08.js';
import { expectSuccess } from '../../../lib/assert.js';
import { expectPaginated } from '../../../lib/pagination.js';
import { waitForCount } from '../../../lib/poll.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-API-08',
  title: 'Привязки групп',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { management, ids, connection } = context;
    const templateId = steps.saveTemplate(ids.name('template'), data.template);
    const groupId = steps.saveGroup(ids.name('group'), [[templateId, 1]]);
    const parties = ids.numbered('party', data.parties);
    const shopId = ids.name('shop');
    expectSuccess(management.savePaymentGroupReferences(groupId, parties.map((partyId) => ({ partyId, shopId }))),
      'save group references');
    const search = (query) => expectSuccess(management.filterPaymentGroupReferences({ searchValue: groupId, ...query }),
      'filter group references');
    waitForCount(connection, 'group references', () => search({ size: 100 }), data.parties);
    const byPrefix = expectSuccess(management.filterPaymentGroupReferences({ searchValue: ids.prefix, size: 100 }),
      'filter group references by prefix');
    context.observe('search_by_prefix_count', (byPrefix.result || []).length);
    const all = search({ size: 100 }).result;
    expectPaginated(search, {
      size: data.pageSize, cursorAfter: (last) => ({ lastId: last.id, sortFieldValue: last.groupId }),
      expectedKeys: all.map((item) => String(item.id)), keyOf: (item) => String(item.id), what: 'group references',
    });
    expectSuccess(management.removePaymentGroupReference(groupId, parties[0], shopId), 'remove group reference');
    waitForCount(connection, 'group references after removal', () => search({ size: 100 }), data.parties - 1);
  },
};
