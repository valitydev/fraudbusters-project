import data from '../../../data/lifecycle/LC-05.js';
import { expectSuccess } from '../../../lib/assert.js';
import { paymentSteps } from '../../payments/steps.js';

export default {
  id: 'LC-05',
  title: 'Шаблон со снятым синтаксисом isTrusted(paymentsConditions(...)) при обновлении',
  applies: (fromVersion) => fromVersion === data.appliesFrom,
  prepare(context) {
    const steps = paymentSteps(context);
    steps.applyTemplate('legacy', data.template, steps.scope(), { ...data.declined, decision: data.expected });
  },
  verify(context) {
    const steps = paymentSteps(context);
    const { ids, management, inspection } = context;
    const stored = (expectSuccess(management.filterPaymentTemplates({ searchValue: ids.name('legacy'), size: 10 }),
      'filter templates').result || [])[0];
    context.observe('legacy_template_in_management', Boolean(stored));
    const answer = inspection.inspectPayment(steps.payment({ ...steps.scope(), ...data.declined }));
    context.observe('legacy_template_decision', answer.status === 200 ? answer.json().result : `http_${answer.status}`);
  },
};
