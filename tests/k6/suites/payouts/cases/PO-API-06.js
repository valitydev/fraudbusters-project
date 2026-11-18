import data from '../../../data/payouts/PO-API-06.js';
import { expectEqual, expectSuccess, expectTrue, sameMembers } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';
import { waitFor } from '../../../lib/poll.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-API-06',
  title: 'Группы шаблонов выплат',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const { payouts, ids, connection } = context;
    const priorities = data.priorities.map((priority, index) => [
      steps.saveTemplate(ids.name(`t${index + 1}`), data.template), priority]);
    const groupId = steps.saveGroup(ids.name('group'), priorities);
    const group = waitFor(connection, 'group is stored', () => {
      const response = payouts.fetchPayoutGroup(groupId, ANY_STATUS);
      return { done: response.status === 200, value: response.json(), observed: response.status };
    });
    sameMembers(group.priorityTemplates.map((item) => `${item.id}:${item.priority}`),
      priorities.map(([id, priority]) => `${id}:${priority}`), 'group templates and priorities');
    const found = expectSuccess(payouts.filterPayoutGroups(ids.prefix), 'filter groups');
    expectTrue((found.result || []).some((item) => item.groupId === groupId), 'group is found by search');
    expectEqual(found.count, (found.result || []).length, 'count equals the number of groups');
    expectEqual(payouts.savePayoutGroup({ priorityTemplates: [{ id: priorities[0][0], priority: 1 }] }, ANY_STATUS).status,
      data.expected.groupWithoutId, 'group without groupId');
    expectEqual(payouts.fetchPayoutGroup(ids.name('missing'), ANY_STATUS).status, data.expected.missingGroup,
      'missing group');
    expectSuccess(payouts.removePayoutGroup(groupId), 'remove group');
    waitFor(connection, 'group is removed', () => ({
      done: payouts.fetchPayoutGroup(groupId, ANY_STATUS).status === data.expected.missingGroup,
    }));
  },
};
