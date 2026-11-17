import data from '../../../data/payouts/PO-API-07.js';
import { expectSuccess } from '../../../lib/assert.js';
import { expectPaginated } from '../../../lib/pagination.js';
import { waitForCount } from '../../../lib/poll.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-API-07',
  title: 'Привязки групп выплат',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const { payouts, ids, connection } = context;
    const templateId = steps.saveTemplate(ids.name('template'), data.template);
    const groupId = steps.saveGroup(ids.name('group'), [[templateId, 1]]);
    const parties = ids.numbered('party', data.partiesWithWallet);
    const walletId = ids.name('wallet');
    const references = [...parties.map((partyId) => ({ partyId, walletId })), { partyId: ids.name('party-only') }];
    expectSuccess(payouts.savePayoutGroupReferences(groupId, references), 'save group references');
    const search = (query) => expectSuccess(payouts.filterPayoutGroupReferences({ searchValue: ids.prefix, ...query }),
      'filter group references');
    waitForCount(connection, 'group references', () => search({ size: 100 }), references.length);
    const all = search({ size: 100 }).result;
    expectPaginated((query) => search({ sortOrder: 'DESC', ...query }), {
      size: data.pageSize, cursorAfter: (last) => ({ lastId: last.id }), expectedKeys: all.map((item) => item.id),
      keyOf: (item) => item.id, order: 'DESC', expectedCount: references.length, what: 'payout group references',
    });
    expectSuccess(payouts.removePayoutGroupReference(groupId, parties[0], walletId), 'remove group reference');
    waitForCount(connection, 'group references after removal', () => search({ size: 100 }), references.length - 1);
  },
};
