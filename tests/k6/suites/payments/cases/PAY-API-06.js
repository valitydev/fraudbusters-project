import data from '../../../data/payments/PAY-API-06.js';
import { expectSuccess, expectTrue, sameMembers } from '../../../lib/assert.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-API-06',
  title: 'Привязки по умолчанию',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { management, ids } = context;
    const templateId = steps.saveTemplate(ids.name('template'), data.template);
    const parties = ids.numbered('party', data.parties);
    const saved = parties.map((partyId) => expectSuccess(management.saveDefaultReference({ partyId, templateId }),
      'save default reference').id);
    expectTrue(saved.every(Boolean), `default reference ids: ${JSON.stringify(saved)}`);
    const partiesOfTemplate = () => (expectSuccess(management.filterDefaultReferences(
      { searchValue: templateId, size: 100 }), 'filter default references').result || [])
      .filter((item) => item.templateId === templateId)
      .map((item) => item.partyId);
    const found = partiesOfTemplate();
    context.observe('found_by_template_id', found.length);
    sameMembers(found, parties, 'default references of the case');
    expectSuccess(management.removeDefaultReference(saved[0]), 'remove default reference');
    sameMembers(partiesOfTemplate(), parties.slice(1), 'default references after removal');
  },
};
