import data from '../../../data/payments/PAY-API-15.js';
import { expectEqual, expectSuccess, expectTrue, sameMembers } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';
import { historyWindow } from '../../../lib/time.js';
import { managementPayment } from '../../../fixtures/payment.js';

export default {
  id: 'PAY-API-15',
  title: 'Наборы данных и применение правила к истории',
  profiles: ['functional'],
  run(context) {
    const { management, ids } = context;
    const partyId = ids.name('party');
    const shopId = ids.name('shop');
    const name = ids.name('data-set');
    const start = Date.now() - data.amounts.length * 60 * 1000;
    const payments = data.amounts.map((amount, index) => managementPayment({
      id: ids.unique('payment'), partyId, shopId, amount, cardToken: ids.name('card'), email: `${ids.prefix}@k6.test`,
      fingerprint: ids.name('fingerprint'), eventTime: new Date(start + index * 60 * 1000).toISOString(),
    }));
    const setId = expectSuccess(management.saveDataSet({ name, rows: payments.map((payment) => ({ payment })) }),
      'save data set').id;
    expectTrue(Boolean(setId), 'data set id is returned');
    const dataSet = expectSuccess(management.fetchDataSet(setId), 'fetch data set');
    expectEqual(dataSet.name, name, 'data set name');
    sameMembers((dataSet.rows || []).map((row) => row.payment && row.payment.paymentId),
      payments.map((payment) => payment.id), 'data set payments');
    context.observe('filter_without_window_status',
      management.filterDataSets({ dataSetName: name, size: 10 }, ANY_STATUS).status);
    const found = expectSuccess(management.filterDataSets({ dataSetName: name, size: 10, ...historyWindow() }),
      'filter data sets');
    expectTrue((found.result || []).some((item) => String(item.id) === String(setId)), 'data set is found by name');

    const checkedId = expectSuccess(management.applyRuleOnHistoricalDataSet({
      dataSetId: Number(setId), records: dataSet.rows.map((row) => row.payment), template: data.template.trim(),
      reference: { partyId, shopId },
    }), 'apply rule on the data set');
    const checked = expectSuccess(management.fetchCheckedDataSet(String(setId)), 'fetch checked data set');
    expectEqual(String(checked.testDataSetId), String(setId), 'checked data set belongs to the data set');
    expectEqual((checked.rows || []).length, payments.length, 'checked rows');
    const byReturnedId = management.fetchCheckedDataSet(String(checkedId), ANY_STATUS);
    context.observe('fetch_by_returned_id_gives_own_set', byReturnedId.status === 200
      && String((byReturnedId.json() || {}).testDataSetId) === String(setId));
    const rowOf = new Map((checked.rows || []).map((row) => [row.payment && row.payment.paymentId, row]));
    const decisions = payments.map((payment) => {
      const row = rowOf.get(payment.id) || {};
      return `${String(row.resultStatus).toLowerCase()}/${row.ruleChecked || ''}`;
    });
    context.observe(`decisions_${data.amounts.join('_')}`, decisions.join(','));
    const bigIndex = data.amounts.findIndex((amount) => amount >= 100000);
    expectEqual(decisions[bigIndex], data.expected.bigAmount, 'amount rule on the emulated payment');
    expectTrue(decisions.filter((decision, index) => index !== bigIndex)
      .every((decision) => data.expected.otherDecisions.includes(decision)),
    `other decisions come from the velocity or the accept rule: ${decisions.join(',')}`);

    expectSuccess(management.removeDataSet(setId), 'remove data set');
    const removed = management.fetchDataSet(setId, ANY_STATUS);
    context.observe('fetch_removed_data_set_status', removed.status);
    expectTrue(removed.status !== 200 || !(removed.json() || {}).rows || removed.json().rows.length === 0,
      'removed data set is not returned');
  },
};
