import data from '../../../data/payouts/PO-FL-04.js';
import { expectSuccess } from '../../../lib/assert.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-04',
  title: 'Глобальная привязка выплат',
  profiles: ['functional'],
  global: true,
  run(context) {
    const steps = payoutSteps(context);
    const { ids, payouts } = context;
    const { expected } = data;
    const target = steps.scope('target');
    const other = steps.scope('other');
    steps.applyTemplate('other-accept', data.otherTemplate, other, { decision: expected.other });
    const globalTemplate = steps.saveTemplate(ids.name('global'), data.globalTemplate);
    const expectGlobalApplied = (what) => {
      steps.awaitDecision(`${what} declines its party`, () => steps.withdrawal(target), expected.targetDeclined);
      steps.expectDecision(steps.withdrawal(other), expected.other, `${what} does not touch other parties`);
    };

    steps.bindTemplate(globalTemplate, { isGlobal: true });
    try {
      expectGlobalApplied('global template');
    } finally {
      expectSuccess(payouts.removePayoutReference({ isGlobal: true }), 'remove global reference');
    }
    steps.awaitDecision('global reference removed', () => steps.withdrawal(target), expected.afterRemoval);

    const groupId = steps.saveGroup(ids.name('global-group'), [[globalTemplate, 1]]);
    steps.bindGroup(groupId, {});
    try {
      expectGlobalApplied('global group');
    } finally {
      expectSuccess(payouts.removePayoutGroupReference(groupId, undefined, undefined), 'remove global group reference');
    }
    steps.awaitDecision('global group reference removed', () => steps.withdrawal(target), expected.afterRemoval);
  },
};
