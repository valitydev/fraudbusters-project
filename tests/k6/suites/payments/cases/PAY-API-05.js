import data from '../../../data/payments/PAY-API-05.js';
import { expectEqual, expectSuccess, expectTrue } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';
import { expectPaginated } from '../../../lib/pagination.js';
import { waitForCount } from '../../../lib/poll.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-API-05',
  title: 'Привязки шаблонов',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { management, ids, connection } = context;
    const templateId = steps.saveTemplate(ids.name('template'), data.template);
    const shopId = ids.name('shop');
    const partyOnly = ids.name('party-only');
    const references = [
      ...ids.numbered('party', data.partiesWithShop).map((partyId) => ({ partyId, shopId, templateId })),
      { partyId: partyOnly, templateId },
    ];
    const saved = expectSuccess(management.savePaymentReferences(references), 'save references');
    expectEqual((saved.result || []).length, references.length, 'saved reference ids');
    const search = (query) => expectSuccess(management.filterPaymentReferences({ searchValue: templateId, ...query }),
      'filter references');
    waitForCount(connection, 'references of the case', () => search({ size: 100 }), references.length);
    const byPrefix = expectSuccess(management.filterPaymentReferences({ searchValue: ids.prefix, size: 100 }),
      'filter references by prefix');
    context.observe('search_by_prefix_count', (byPrefix.result || []).length);
    const all = search({ size: 100 }).result;
    expectTrue(all.some((reference) => !reference.shopId && reference.partyId === partyOnly),
      'party-only reference is stored without shop');
    expectPaginated(search, {
      size: data.pageSize, cursorAfter: (last) => ({ lastId: last.id, sortFieldValue: last.templateId }),
      expectedKeys: all.map((reference) => reference.id), keyOf: (item) => item.id, expectedCount: references.length,
      what: 'references',
    });
    expectSuccess(management.removePaymentReference(all[0].id), 'remove reference');
    waitForCount(connection, 'references after removal', () => search({ size: 100 }), references.length - 1);
    const missing = management.savePaymentReferences(
      [{ partyId: ids.name('party-missing'), shopId, templateId: ids.name('missing') }], ANY_STATUS);
    context.observe('reference_to_missing_template_status', missing.status);
    expectTrue(missing.status < 500, `reference to a missing template: HTTP ${missing.status}`);
  },
};
