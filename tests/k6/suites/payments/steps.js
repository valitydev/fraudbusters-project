import { expectSuccess } from '../../lib/assert.js';
import { waitFor } from '../../lib/poll.js';
import { historyWindow } from '../../lib/time.js';
import { ruleSteps } from '../../lib/rules.js';
import { buildPayment, paymentChange } from '../../fixtures/payment.js';

export const REST_RESULT = {
  accept: 'low', accept_and_notify: 'low', notify: 'low', decline: 'fatal', decline_and_notify: 'fatal',
  three_ds: 'high', high_risk: 'high',
};

export function inspectResultId(paymentId) {
  return `${paymentId}.${paymentId}`;
}

const same = (left, right) => (left || undefined) === (right || undefined);

export function paymentSteps(context) {
  const { management, inspection, ids, connection } = context;

  const findPaymentsHistory = (filter) => expectSuccess(management.filterPaymentsHistory(
    { ...historyWindow(), size: 100, ...filter }), 'filter payments history').result || [];

  const channel = {
    kind: 'payment',
    successStatus: 'captured',
    scope: (suffix) => ({ partyId: ids.name(`party${suffix}`), shopId: ids.name(`shop${suffix}`) }),
    referenceScope: ({ partyId, shopId }) => ({ partyId, shopId }),
    sameScope: (item, scope) => same(item.partyId, scope.partyId) && same(item.shopId, scope.shopId),
    saveTemplate: (template) => management.savePaymentTemplate(template),
    findTemplate: (templateId) => (expectSuccess(management.filterPaymentTemplates(
      { searchValue: templateId, size: 10 }), 'filter templates').result || []).find((item) => item.id === templateId),
    saveReferences: (references) => management.savePaymentReferences(references),
    findReferences: (templateId) => (expectSuccess(management.filterPaymentReferences(
      { searchValue: templateId, size: 100 }), 'filter references').result || [])
      .filter((item) => item.templateId === templateId),
    saveGroup: (group) => management.savePaymentGroup(group),
    saveGroupReferences: (groupId, references) => management.savePaymentGroupReferences(groupId, references),
    build: buildPayment,
    inspect: (payment) => expectSuccess(inspection.inspectPayment(payment), `inspect payment ${payment.id}`).result,
    load: (payments, status, errorCode) => inspection.loadPaymentChanges(payments.map((payment) => paymentChange(
      payment, status, errorCode ? { errorCode, errorMessage: 'k6' } : undefined))),
    findHistory: (payment) => findPaymentsHistory({ partyId: payment.merchant.id, paymentId: payment.id }),
    saveListRows: (listType, records) => management.savePaymentListRows(listType, records),
    findListRows: (listType, listName, searchValue) => expectSuccess(management.filterPaymentLists({
      listType, listNames: [listName], searchValue, size: 100,
    }), `filter ${listType} list ${listName}`).result || [],
    rowValue: (row) => row.value,
    rowScope: (row) => ({ partyId: row.partyId, shopId: row.shopId }),
  };

  const steps = ruleSteps(context, channel);

  const inspectResults = (filter) => expectSuccess(management.filterInspectResults(
    { ...historyWindow(), size: 100, ...filter }), 'filter inspect results').result || [];

  return Object.assign(steps, {
    payment: steps.operation,
    findReferences: channel.findReferences,
    findPaymentsHistory,
    inspectResults,

    awaitReferenceIds(templateId, expected) {
      return waitFor(connection, `${expected} references to ${templateId}`, () => {
        const found = channel.findReferences(templateId);
        return { done: found.length === expected, value: found.map((item) => item.id), observed: found.length };
      });
    },

    awaitInspectResult(payment) {
      const resultId = inspectResultId(payment.id);
      return waitFor(connection, `inspect result of ${payment.id}`, () => {
        const found = inspectResults({ partyId: payment.merchant.id, paymentId: resultId })
          .find((item) => item.payment && item.payment.id === resultId);
        return { done: Boolean(found), value: found };
      });
    },

    inspectAndReadStored(payment) {
      const response = inspection.inspectPayment(payment);
      const rest = response.status === 200 ? response.json().result : `http_${response.status}`;
      const stored = steps.awaitInspectResult(payment);
      const status = String(stored.resultStatus).toLowerCase();
      return { rest, status, rule: stored.ruleChecked || '', stored };
    },

    storedDecisionOutcomes(scenarios) {
      return scenarios.flatMap(({ name, payment, expected }) => {
        const { rest, status, rule } = steps.inspectAndReadStored(payment);
        const outcomes = [{ name, actual: `${status}/${rule}`, expected }];
        if (REST_RESULT[status]) outcomes.push({ name: `${name} (REST)`, actual: rest, expected: REST_RESULT[status] });
        return outcomes;
      });
    },
  });
}
