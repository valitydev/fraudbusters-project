import { expectSuccess, fail } from '../../lib/assert.js';
import { historyWindow } from '../../lib/time.js';
import { ruleSteps } from '../../lib/rules.js';
import { buildWithdrawal, withdrawalRecord } from '../../fixtures/withdrawal.js';

const same = (left, right) => (left || undefined) === (right || undefined);

export function payoutSteps(context) {
  const { payouts, payoutInspector, inspection, ids } = context;

  const findWithdrawalsHistory = (filter) => expectSuccess(payouts.filterWithdrawalsHistory(
    { ...historyWindow(), size: 100, ...filter }), 'filter withdrawals history').result || [];

  const channel = {
    kind: 'withdrawal',
    successStatus: 'succeeded',
    scope: (suffix) => ({ partyId: ids.name(`party${suffix}`), walletId: ids.name(`wallet${suffix}`) }),
    referenceScope: ({ partyId, walletId, isGlobal }) => (isGlobal ? { isGlobal } : { partyId, walletId }),
    sameScope: (item, scope) => (scope.isGlobal
      ? item.isGlobal === true
      : same(item.partyId, scope.partyId) && same(item.walletId, scope.walletId)),
    saveTemplate: (template) => payouts.savePayoutTemplate(template),
    findTemplate: (templateId) => (expectSuccess(payouts.filterPayoutTemplates(
      { searchValue: templateId, size: 10 }), 'filter payout templates').result || [])
      .find((item) => item.id === templateId),
    saveReferences: (references) => payouts.savePayoutReferences(references),
    findReferences: (templateId, scope = {}) => (expectSuccess(payouts.filterPayoutReferences(
      { searchValue: templateId, isGlobal: Boolean(scope.isGlobal), size: 100 }), 'filter payout references').result
      || []).filter((item) => item.templateId === templateId),
    saveGroup: (group) => payouts.savePayoutGroup(group),
    saveGroupReferences: (groupId, references) => payouts.savePayoutGroupReferences(groupId, references),
    build: buildWithdrawal,
    inspect: (withdrawal) => {
      const answer = payoutInspector.inspectWithdrawal(withdrawal);
      if (answer.score === undefined) {
        fail(`inspect withdrawal ${withdrawal.id}: ${answer.failure
          || `InvalidRequest ${JSON.stringify(answer.invalidRequest)}`}`);
      }
      return answer.score;
    },
    load: (withdrawals, status, errorCode) => inspection.loadWithdrawals(
      withdrawals.map((withdrawal) => withdrawalRecord(withdrawal, status, errorCode))),
    findHistory: (withdrawal) => findWithdrawalsHistory({ partyId: withdrawal.partyId, withdrawalId: withdrawal.id })
      .filter((item) => item.id === withdrawal.id),
    saveListRows: (listType, records) => payouts.savePayoutListRows(listType, records),
    findListRows: (listType, listName, searchValue) => expectSuccess(payouts.filterPayoutLists({
      listType, listNames: [listName], searchValue, size: 100,
    }), `filter payout ${listType} list ${listName}`).result || [],
    rowValue: (row) => row.listRecord && row.listRecord.value,
    rowScope: (row) => ({ partyId: row.listRecord.partyId, walletId: row.listRecord.walletId }),
  };

  const steps = ruleSteps(context, channel);

  const findGlobalReference = () => (expectSuccess(payouts.filterPayoutReferences({ isGlobal: true, size: 100 }),
    'filter global payout references').result || []).find((item) => item.isGlobal === true);

  const findGlobalGroupReference = (lastId) => {
    const page = expectSuccess(payouts.filterPayoutGroupReferences({ size: 100, sortOrder: 'ASC', lastId }),
      'filter payout group references').result || [];
    const global = page.find((item) => !item.partyId && !item.walletId);
    if (global || page.length < 100) return global;
    return findGlobalGroupReference(page[page.length - 1].id);
  };

  const restoreGlobalReference = (original) => {
    if (original) {
      steps.bindTemplate(original.templateId, { isGlobal: true });
    } else {
      expectSuccess(payouts.removePayoutReference({ isGlobal: true }), 'remove global reference');
    }
  };

  const restoreGlobalGroupReference = (original, groupId) => {
    if (original) {
      steps.bindGroup(original.groupId, {});
    } else {
      expectSuccess(payouts.removePayoutGroupReference(groupId, undefined, undefined), 'remove global group reference');
    }
  };

  return Object.assign(steps, {
    withdrawal: steps.operation,
    findWithdrawalsHistory,
    findGlobalReference,
    findGlobalGroupReference,
    restoreGlobalReference,
    restoreGlobalGroupReference,
  });
}
