import data from '../../../data/payouts/PO-API-04.js';
import { expectSuccess, sameMembers } from '../../../lib/assert.js';
import { waitFor } from '../../../lib/poll.js';

export default {
  id: 'PO-API-04',
  title: 'Имена шаблонов выплат',
  profiles: ['functional'],
  run(context) {
    const { payouts, management, ids, connection } = context;
    const prefix = ids.name('name');
    const templates = ids.numbered('name', data.templates);
    templates.forEach((id) => expectSuccess(payouts.savePayoutTemplate({ id, template: data.template }), `save ${id}`));
    expectSuccess(management.savePaymentTemplate({ id: `${prefix}-payment`, template: data.template }),
      'save payment template with the same prefix');
    const names = waitFor(connection, 'payout template names', () => {
      const result = expectSuccess(payouts.findPayoutTemplateNames(`${prefix}%`), 'payout template names').result || [];
      return { done: result.length >= data.templates, value: result, observed: result.length };
    });
    sameMembers(names, templates, 'payout template names without payment templates');
  },
};
