import data from '../../../data/payouts/PO-API-05.js';
import { expectEqual, expectSuccess, expectTrue } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';
import { expectPaginated } from '../../../lib/pagination.js';
import { waitFor, waitForCount } from '../../../lib/poll.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-API-05',
  title: 'Привязки шаблонов выплат',
  profiles: ['functional'],
  global: true,
  run(context) {
    const steps = payoutSteps(context);
    const { payouts, ids, connection } = context;
    const parties = ids.numbered('party', data.partiesWithWallet);
    const walletId = ids.name('wallet');
    const partyOnly = ids.name('party-only');
    const templateId = steps.saveTemplate(ids.name('template'), data.template);
    const references = [...parties.map((partyId) => ({ templateId, partyId, walletId })), { templateId, partyId: partyOnly }];
    expectEqual((expectSuccess(payouts.savePayoutReferences(references), 'save references').result || []).length,
      references.length, 'saved reference ids');
    const withoutParty = payouts.savePayoutReferences([{ templateId, walletId }], ANY_STATUS);
    context.observe('wallet_without_party_status', withoutParty.status);
    expectEqual(withoutParty.status, data.expected.walletWithoutParty, 'wallet without party');

    const search = (query) => expectSuccess(payouts.filterPayoutReferences({ searchValue: ids.prefix, ...query }),
      'filter payout references');
    waitForCount(connection, 'references of the case', () => search({ size: 100 }), references.length);
    const all = search({ size: 100 }).result;
    expectTrue(all.some((item) => item.partyId === partyOnly && !item.walletId), 'party-only reference has no wallet');
    expectPaginated((query) => search({ sortOrder: 'ASC', ...query }), {
      size: data.pageSize, cursorAfter: (last) => ({ lastId: last.id }), expectedKeys: all.map((item) => item.id),
      keyOf: (item) => item.id, order: 'ASC', expectedCount: references.length, what: 'payout references',
    });

    steps.bindTemplate(templateId, { isGlobal: true });
    try {
      const globals = waitFor(connection, 'global reference', () => {
        const result = expectSuccess(payouts.filterPayoutReferences({ isGlobal: true, size: 100 }), 'filter global')
          .result || [];
        const own = result.filter((item) => item.templateId === templateId).length;
        return { done: own === 1, value: result, observed: own };
      });
      expectTrue(globals.every((item) => item.isGlobal === true), 'isGlobal=true returns only global references');
      expectTrue(search({ isGlobal: false, size: 100 }).result.every((item) => item.isGlobal !== true),
        'isGlobal=false returns no global references');
    } finally {
      expectSuccess(payouts.removePayoutReference({ isGlobal: true }), 'remove global reference');
    }
    expectSuccess(payouts.removePayoutReference({ partyId: parties[0], walletId }), 'remove reference by scope');
    waitForCount(connection, 'references after removal', () => search({ size: 100 }), references.length - 1);
    const missing = payouts.removePayoutReference({ partyId: ids.name('missing'), walletId }, ANY_STATUS);
    context.observe('remove_missing_status', missing.status);
    expectEqual(missing.status, data.expected.removeMissing, 'remove a missing reference');
  },
};
