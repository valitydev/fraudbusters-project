import data from '../../../data/payouts/PO-FL-10.js';
import { expectEqual, expectSuccess, expectTrue, validationErrors } from '../../../lib/assert.js';

export default {
  id: 'PO-FL-10',
  title: 'Запрещённые конструкции в шаблоне выплаты',
  profiles: ['functional'],
  run(context) {
    const { payouts, management, ids } = context;
    const accepted = Object.entries(data.forbidden).filter(([name, template]) => {
      const errors = validationErrors(expectSuccess(payouts.validatePayoutTemplate({ id: ids.name(name), template }),
        `validate ${name}`));
      context.observe(name, errors[0] || 'no error');
      return errors.length === 0;
    }).map(([name]) => name);
    expectEqual(accepted, [], 'forbidden payout constructs pass validation');
    const savedId = ids.name('saved-forbidden');
    const saved = expectSuccess(payouts.savePayoutTemplate({ id: savedId, template: data.forbidden[data.savedForbidden] }),
      'save a forbidden payout template');
    expectTrue(Array.isArray(saved.errors) && saved.errors.length > 0,
      `forbidden template must return errors: ${JSON.stringify(saved)}`);
    expectEqual((expectSuccess(payouts.filterPayoutTemplates({ searchValue: savedId, size: 10 }),
      'filter payout templates').result || []).length, 0, 'forbidden template is not stored');
    const paymentErrors = validationErrors(expectSuccess(management.validatePaymentTemplate({
      id: ids.name('payment-template'), template: data.forbidden[data.validForPayments],
    }), 'validate the construct in a payment template'));
    expectEqual(paymentErrors, [], `${data.validForPayments} is valid for payments`);
  },
};
