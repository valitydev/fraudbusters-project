import data from '../../../data/payments/PAY-API-04.js';
import { expectSuccess, sameMembers } from '../../../lib/assert.js';
import { waitFor } from '../../../lib/poll.js';

export default {
  id: 'PAY-API-04',
  title: 'Имена шаблонов',
  profiles: ['functional'],
  run(context) {
    const { management, ids, connection } = context;
    const templates = ids.numbered('name', data.templates);
    templates.forEach((id) => expectSuccess(management.savePaymentTemplate({ id, template: data.template }),
      `save ${id}`));
    const names = waitFor(connection, 'template names', () => {
      const result = expectSuccess(management.findPaymentTemplateNames(`${ids.name('name')}%`), 'template names')
        .result || [];
      return { done: result.length === data.templates, value: result, observed: result.length };
    });
    sameMembers(names, templates, 'template names');
  },
};
