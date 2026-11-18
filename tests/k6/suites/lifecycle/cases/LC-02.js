import data from '../../../data/lifecycle/LC-02.js';
import { paymentSteps } from '../../payments/steps.js';

export default {
  id: 'LC-02',
  title: 'Группа шаблонов платежа после перезапуска',
  prepare(context) {
    const steps = paymentSteps(context);
    const { ids } = context;
    const scope = steps.scope();
    const templateId = steps.saveTemplate(ids.name('decline'), data.template);
    steps.bindGroup(steps.saveGroup(ids.name('group'), [[templateId, 1]]), { partyId: scope.partyId });
    steps.awaitDecision('group applied', () => steps.payment(scope), data.expected);
  },
  verify(context) {
    const steps = paymentSteps(context);
    steps.awaitDecision('group restored', () => steps.payment(steps.scope()), data.expected);
  },
};
