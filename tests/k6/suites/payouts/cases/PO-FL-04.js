import data from '../../../data/payouts/PO-FL-04.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-04',
  title: 'Глобальная привязка выплат',
  profiles: ['functional'],
  global: true,
  run(context) {
    const steps = payoutSteps(context);
    const { ids } = context;
    const { expected } = data;
    const target = steps.scope('target');
    const other = steps.scope('other');
    steps.applyTemplate('other-accept', data.otherTemplate, other, { decision: expected.other });
    const globalTemplate = steps.saveTemplate(ids.name('global'), data.globalTemplate);
    const expectGlobalApplied = (what) => {
      steps.awaitDecision(`${what} declines its party`, () => steps.withdrawal(target), expected.targetDeclined);
      steps.expectDecision(steps.withdrawal(other), expected.other, `${what} does not touch other parties`);
    };

    const originalReference = steps.findGlobalReference();
    const originalGroupReference = steps.findGlobalGroupReference();
    const awaitWithoutGlobals = (what) => {
      if (!originalReference && !originalGroupReference) {
        steps.awaitDecision(what, () => steps.withdrawal(target), expected.afterRemoval);
      }
    };

    steps.bindTemplate(globalTemplate, { isGlobal: true });
    try {
      expectGlobalApplied('global template');
    } finally {
      steps.restoreGlobalReference(originalReference);
    }
    awaitWithoutGlobals('global reference removed');

    const groupId = steps.saveGroup(ids.name('global-group'), [[globalTemplate, 1]]);
    steps.bindGroup(groupId, {});
    try {
      expectGlobalApplied('global group');
    } finally {
      steps.restoreGlobalGroupReference(originalGroupReference, groupId);
    }
    awaitWithoutGlobals('global group reference removed');
  },
};
