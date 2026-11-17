import data from '../../../data/payments/PAY-API-01.js';
import { expectEqual, expectSuccess, expectTrue, validationErrors } from '../../../lib/assert.js';

export default {
  id: 'PAY-API-01',
  title: 'Валидация шаблона',
  profiles: ['functional'],
  run(context) {
    const { management, ids } = context;
    const validate = (role, template) => validationErrors(expectSuccess(
      management.validatePaymentTemplate({ id: ids.name(role), template }), `validate ${role} template`));
    expectEqual(validate('valid', data.valid), [], 'errors of a valid template');
    const invalid = validate('invalid', data.invalid);
    expectTrue(invalid.length > 0 && invalid.every((error) => String(error).length > 0),
      `invalid template must have error texts, got ${JSON.stringify(invalid)}`);
  },
};
