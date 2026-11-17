import data from '../../../data/payments/PAY-API-07.js';
import { expectSuccess, expectTrue, sameMembers } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';
import { waitFor } from '../../../lib/poll.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-API-07',
  title: 'Группы шаблонов',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { management, ids, connection } = context;
    const priorities = data.priorities.map((priority, index) => [
      steps.saveTemplate(ids.name(`t${index + 1}`), data.template), priority]);
    const groupId = steps.saveGroup(ids.name('group'), priorities);
    const group = waitFor(connection, 'group is stored', () => {
      const response = management.fetchPaymentGroup(groupId, ANY_STATUS);
      return { done: response.status === 200, value: response.json(), observed: response.status };
    });
    sameMembers(group.priorityTemplates.map((item) => `${item.id}:${item.priority}`),
      priorities.map(([id, priority]) => `${id}:${priority}`), 'group templates and priorities');
    const groupFound = () => (expectSuccess(management.filterPaymentGroups(groupId), 'filter groups').result || [])
      .some((item) => item.groupId === groupId);
    expectTrue(groupFound(), 'group is found by search');
    expectSuccess(management.removePaymentGroup(groupId), 'remove group');
    waitFor(connection, 'group is removed', () => ({ done: !groupFound() }));
    context.observe('fetch_removed_group_status', management.fetchPaymentGroup(groupId, ANY_STATUS).status);
  },
};
