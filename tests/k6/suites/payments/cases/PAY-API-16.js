import data from '../../../data/payments/PAY-API-16.js';
import { expectSuccess, expectTrue, sameMembers } from '../../../lib/assert.js';
import { waitFor } from '../../../lib/poll.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-API-16',
  title: 'Эмуляция каскада шаблонов',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { management, ids, connection } = context;
    const scope = steps.scope();
    const [groupTemplate, partyTemplate, shopTemplate] = data.cascade.map((role) => steps.saveTemplate(ids.name(role),
      data.template));
    steps.bindGroup(steps.saveGroup(ids.name('group'), [[groupTemplate, 1]]), { partyId: scope.partyId });
    steps.bindTemplate(partyTemplate, { partyId: scope.partyId });
    steps.bindTemplate(shopTemplate, scope);
    const flow = waitFor(connection, 'emulated cascade', () => {
      const templates = (expectSuccess(management.emulateTemplatesFlow(scope.partyId, scope.shopId),
        'emulate templates flow').result || []).map((item) => item.id);
      return { done: templates.length >= data.cascade.length, value: templates, observed: templates.join(',') };
    });
    context.observe('emulated_order', flow.map((id) => id.replace(`${ids.prefix}-`, '')).join(','));
    sameMembers(flow, [groupTemplate, partyTemplate, shopTemplate], 'templates of the cascade');
    expectTrue(flow.indexOf(partyTemplate) < flow.indexOf(shopTemplate),
      `party template before party+shop template: ${flow.join(',')}`);
  },
};
