import data from '../../../data/payouts/PO-API-01.js';
import { expectEqual, expectSuccess, expectTrue, validationErrors } from '../../../lib/assert.js';

export default {
  id: 'PO-API-01',
  title: 'Валидация шаблона выплаты',
  profiles: ['functional'],
  run(context) {
    const { payouts, ids } = context;
    const validate = (role) => validationErrors(expectSuccess(payouts.validatePayoutTemplate(
      { id: ids.name(role), template: data[role].trim() }), `validate ${role}`));
    expectEqual(validate('valid'), [], 'errors of a valid template');
    expectEqual(validate('withPayoutHistory'), [], 'errors of a template with payout history');
    expectTrue(validate('invalid').length > 0, 'invalid template must have error texts');
  },
};
