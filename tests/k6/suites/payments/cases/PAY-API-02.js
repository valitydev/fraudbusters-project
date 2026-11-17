import data from '../../../data/payments/PAY-API-02.js';
import { expectEqual, expectSuccess, expectTrue } from '../../../lib/assert.js';
import { waitFor, waitForCount } from '../../../lib/poll.js';

export default {
  id: 'PAY-API-02',
  title: 'Создание, обновление, удаление шаблона',
  profiles: ['functional'],
  run(context) {
    const { management, ids, connection } = context;
    const search = (searchValue) => expectSuccess(management.filterPaymentTemplates({ searchValue, size: 10 }),
      'filter templates');
    const id = ids.name('template');
    const saved = expectSuccess(management.savePaymentTemplate({ id, template: data.template }), 'save template');
    expectEqual([saved.id, saved.template], [id, data.template], 'saved template');
    waitForCount(connection, 'saved template', () => search(id), 1);
    management.savePaymentTemplate({ id, template: data.changed });
    waitFor(connection, 'template text is replaced', () => {
      const found = (search(id).result || [])[0];
      return { done: Boolean(found) && found.template === data.changed, observed: found && found.template };
    });
    const invalidId = ids.name('invalid');
    const invalid = expectSuccess(management.savePaymentTemplate({ id: invalidId, template: data.invalid }),
      'save invalid template');
    expectTrue(Array.isArray(invalid.errors) && invalid.errors.length > 0,
      `invalid template must return errors, got ${JSON.stringify(invalid)}`);
    expectEqual(search(invalidId).result.length, 0, 'invalid template is not stored');
    expectSuccess(management.removePaymentTemplate(id), 'remove template');
    waitForCount(connection, 'removed template', () => search(id), 0);
  },
};
