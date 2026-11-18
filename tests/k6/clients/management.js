import http from 'k6/http';
import { authorizationHeaders } from '../lib/auth.js';
import { send } from '../lib/http.js';

export function managementClient(connection) {
  const base = connection.managementUrl;
  const call = (method, path, options) => send(connection, method, `${base}${path}`, options);
  return {
    validatePaymentTemplate: (template) => call('POST', '/payments-templates/validate',
      { body: template, stage: 'mgmt_validate_payment_template' }),
    savePaymentTemplate: (template) => call('POST', '/payments-templates',
      { body: template, stage: 'mgmt_save_payment_template' }),
    removePaymentTemplate: (id) => call('DELETE', `/payments-templates/${encodeURIComponent(id)}`,
      { stage: 'mgmt_remove_payment_template' }),
    filterPaymentTemplates: (query, expectedStatuses) => call('GET', '/payments-templates/filter',
      { query, stage: 'mgmt_filter_payment_templates', expectedStatuses }),
    findPaymentTemplateNames: (regexpName) => call('GET', '/payments-templates/names',
      { query: { regexpName }, stage: 'mgmt_payment_template_names' }),

    savePaymentReferences: (references, expectedStatuses) => call('POST', '/payments-references',
      { body: references, stage: 'mgmt_save_payment_references', expectedStatuses }),
    removePaymentReference: (id) => call('DELETE', `/payments-references/${encodeURIComponent(id)}`,
      { stage: 'mgmt_remove_payment_reference' }),
    filterPaymentReferences: (query) => call('GET', '/payments-references/filter',
      { query, stage: 'mgmt_filter_payment_references' }),

    savePaymentGroup: (group) => call('POST', '/payments-groups',
      { body: group, stage: 'mgmt_save_payment_group' }),
    fetchPaymentGroup: (groupId, expectedStatuses) => call('GET', `/payments-groups/${encodeURIComponent(groupId)}`,
      { stage: 'mgmt_fetch_payment_group', expectedStatuses }),
    filterPaymentGroups: (searchValue) => call('GET', '/payments-groups/filter',
      { query: { searchValue }, stage: 'mgmt_filter_payment_groups' }),
    removePaymentGroup: (groupId) => call('DELETE', `/payments-groups/${encodeURIComponent(groupId)}`,
      { stage: 'mgmt_remove_payment_group' }),
    savePaymentGroupReferences: (groupId, references) => call('POST',
      `/payments-groups/${encodeURIComponent(groupId)}/references`,
      { body: references, stage: 'mgmt_save_payment_group_references' }),
    removePaymentGroupReference: (groupId, partyId, shopId) => call('DELETE',
      `/payments-groups/${encodeURIComponent(groupId)}/references/remove`,
      { query: { groupId, partyId, shopId }, stage: 'mgmt_remove_payment_group_reference' }),
    filterPaymentGroupReferences: (query) => call('GET', '/payments-groups/references/filter',
      { query, stage: 'mgmt_filter_payment_group_references' }),

    savePaymentListRows: (listType, records) => call('POST', '/payments-lists',
      { body: { listType, records }, stage: 'mgmt_save_payment_list_rows' }),
    removePaymentListRow: (id) => call('DELETE', `/payments-lists/${encodeURIComponent(id)}`,
      { stage: 'mgmt_remove_payment_list_row' }),
    filterPaymentLists: (query, expectedStatuses) => call('GET', '/payments-lists/filter',
      { query, stage: 'mgmt_filter_payment_lists', expectedStatuses }),
    fetchAvailablePaymentListNames: () => call('GET', '/payments-lists/availableListNames',
      { stage: 'mgmt_available_payment_list_names' }),
    fetchCurrentPaymentListNames: (listType) => call('GET', '/payments-lists/currentListNames',
      { query: { listType }, stage: 'mgmt_current_payment_list_names' }),

    filterPaymentsHistory: (query, expectedStatuses) => call('GET', '/payments-historical-data/payments-info',
      { query, stage: 'mgmt_filter_payments_history', expectedStatuses }),
    filterRefundsHistory: (query) => call('GET', '/payments-historical-data/refunds',
      { query, stage: 'mgmt_filter_refunds_history' }),
    filterChargebacksHistory: (query) => call('GET', '/payments-historical-data/chargebacks',
      { query, stage: 'mgmt_filter_chargebacks_history' }),
    filterFraudPaymentsHistory: (query) => call('GET', '/payments-historical-data/fraud-payments',
      { query, stage: 'mgmt_filter_fraud_payments_history' }),
    filterInspectResults: (query) => call('GET', '/payments-historical-data/inspect-results',
      { query, stage: 'mgmt_filter_inspect_results' }),

    saveDefaultReference: (reference) => call('POST', '/payments-references/default',
      { body: reference, stage: 'mgmt_save_default_reference' }),
    filterDefaultReferences: (query) => call('GET', '/payments-references/default/filter',
      { query, stage: 'mgmt_filter_default_references' }),
    removeDefaultReference: (id) => call('DELETE', `/payments-references/default/${encodeURIComponent(id)}`,
      { stage: 'mgmt_remove_default_reference' }),

    insertListCandidates: (records) => call('POST', '/payments-lists/candidates',
      { body: { records }, stage: 'mgmt_insert_list_candidates' }),
    filterListCandidates: (query) => call('GET', '/payments-lists/candidates',
      { query, stage: 'mgmt_filter_list_candidates' }),
    filterCandidateBatches: (query) => call('GET', '/payments-lists/candidates-batches',
      { query, stage: 'mgmt_filter_candidate_batches' }),
    approveListCandidates: (ids) => call('POST', '/payments-lists/candidates/approved',
      { body: { ids }, stage: 'mgmt_approve_list_candidates' }),

    fetchAnalytics: (name, query, expectedStatuses) => call('GET', `/analytics/${name}`,
      { query, stage: `mgmt_analytics_${name.replace(/[^a-z]+/g, '_')}`, expectedStatuses }),

    filterAudit: (query) => call('GET', '/audit/filter', { query, stage: 'mgmt_filter_audit' }),
    fetchAuditCommandTypes: () => call('GET', '/audit/commandTypes', { stage: 'mgmt_audit_command_types' }),
    fetchAuditObjectTypes: () => call('GET', '/audit/objectTypes', { stage: 'mgmt_audit_object_types' }),

    uploadPaymentListCsv: (listType, csv) => multipart(connection, `${base}/payments-lists/csv`, listType, csv,
      'mgmt_upload_payment_list_csv'),
    removePaymentListCsv: (listType, csv) => multipart(connection, `${base}/payments-lists/remove/csv`, listType, csv,
      'mgmt_remove_payment_list_csv'),

    saveDataSet: (dataSet) => call('POST', '/payments-data-set/data-sets',
      { body: dataSet, stage: 'mgmt_save_data_set' }),
    fetchDataSet: (setId, expectedStatuses) => call('GET', `/payments-data-set/data-sets/${encodeURIComponent(setId)}`,
      { stage: 'mgmt_fetch_data_set', expectedStatuses }),
    filterDataSets: (query, expectedStatuses) => call('GET', '/payments-data-set/data-sets/filter',
      { query, stage: 'mgmt_filter_data_sets', expectedStatuses }),
    removeDataSet: (setId) => call('DELETE', `/payments-data-set/data-sets/${encodeURIComponent(setId)}`,
      { stage: 'mgmt_remove_data_set' }),
    applyRuleOnHistoricalDataSet: (request) => call('POST', '/payments-data-set/data-sets/applyRuleOnHistoricalDataSet',
      { body: request, stage: 'mgmt_apply_rule_on_data_set' }),
    fetchCheckedDataSet: (id, expectedStatuses) => call('GET',
      `/payments-data-set/checked-data-sets/${encodeURIComponent(id)}`,
      { stage: 'mgmt_fetch_checked_data_set', expectedStatuses }),
    emulateTemplatesFlow: (partyId, shopId) => call('GET', '/payments-emulations',
      { query: { partyId, shopId }, stage: 'mgmt_emulate_templates_flow' }),

    uploadFraudPaymentsCsv: (csv) => {
      const response = http.post(`${base}/payments-load-data/fraud-payments`,
        { file: http.file(csv, 'fraud-payments.csv', 'text/csv') },
        { timeout: connection.requestTimeout, headers: authorizationHeaders(),
          tags: { stage: 'mgmt_upload_fraud_payments', name: 'mgmt_upload_fraud_payments' } });
      return { status: response.status, body: response.body, url: 'POST /payments-load-data/fraud-payments',
        json: () => (response.body ? JSON.parse(response.body) : undefined) };
    },
  };
}

function multipart(connection, url, listType, csv, stage) {
  const response = http.post(url, { listType, file: http.file(csv, 'list.csv', 'text/csv') },
    { timeout: connection.requestTimeout, headers: authorizationHeaders(), tags: { stage, name: stage } });
  return { status: response.status, body: response.body, url: `POST ${url}`,
    json: () => (response.body ? JSON.parse(response.body) : undefined) };
}
