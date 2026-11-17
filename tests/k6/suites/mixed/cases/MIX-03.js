import data from '../../../data/mixed/MIX-03.js';
import { expectEqual, expectSuccess, validationErrors } from '../../../lib/assert.js';

export default {
  id: 'MIX-03',
  title: 'Валидация функций выплат в шаблоне платежа',
  profiles: ['functional'],
  run(context) {
    const body = expectSuccess(context.management.validatePaymentTemplate({
      id: context.ids.name('t'), template: data.paymentTemplateWithPayoutFunctions.trim(),
    }), 'validate payment template with payout functions');
    expectEqual(validationErrors(body), [], 'validation errors');
  },
};
