import data from '../../../data/payouts/PO-API-10.js';
import { expectSuccess } from '../../../lib/assert.js';
import { awaitAuditRecords } from '../../audit.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-API-10',
  title: 'Аудит команд выплат',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const { payouts, ids } = context;
    const party = { partyId: ids.name('party') };
    const templateId = steps.bindNewTemplate('audited', data.template, party);
    steps.bindGroup(steps.saveGroup(ids.name('group'), [[templateId, 1]]), party);
    steps.addListRow('black', 'EMAIL', `${ids.prefix}@k6.test`, party);
    expectSuccess(payouts.removePayoutTemplate(templateId), 'remove payout template');
    const records = awaitAuditRecords(context, data, ids.prefix);
    context.observe('kinds', [...new Set(records.map((item) => `${item.objectType}:${item.commandType}`))].sort().join(','));
  },
};
