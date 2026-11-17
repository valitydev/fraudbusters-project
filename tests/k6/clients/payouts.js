import { send } from '../lib/http.js';

export function payoutManagementClient(connection) {
  const base = connection.managementUrl;
  const call = (method, path, options) => send(connection, method, `${base}${path}`, options);
  return {
    validatePayoutTemplate: (template) => call('POST', '/payouts-templates/validate',
      { body: template, stage: 'mgmt_validate_payout_template' }),
    savePayoutTemplate: (template) => call('POST', '/payouts-templates',
      { body: template, stage: 'mgmt_save_payout_template' }),
    removePayoutTemplate: (id) => call('DELETE', `/payouts-templates/${encodeURIComponent(id)}`,
      { stage: 'mgmt_remove_payout_template' }),
    filterPayoutTemplates: (query, expectedStatuses) => call('GET', '/payouts-templates/filter',
      { query, stage: 'mgmt_filter_payout_templates', expectedStatuses }),
    findPayoutTemplateNames: (regexpName) => call('GET', '/payouts-templates/names',
      { query: { regexpName }, stage: 'mgmt_payout_template_names' }),

    savePayoutReferences: (references, expectedStatuses) => call('POST', '/payouts-references',
      { body: references, stage: 'mgmt_save_payout_references', expectedStatuses }),
    removePayoutReference: (query, expectedStatuses) => call('DELETE', '/payouts-references',
      { query, stage: 'mgmt_remove_payout_reference', expectedStatuses }),
    filterPayoutReferences: (query) => call('GET', '/payouts-references/filter',
      { query, stage: 'mgmt_filter_payout_references' }),

    savePayoutGroup: (group, expectedStatuses) => call('POST', '/payouts-groups',
      { body: group, stage: 'mgmt_save_payout_group', expectedStatuses }),
    fetchPayoutGroup: (groupId, expectedStatuses) => call('GET', `/payouts-groups/${encodeURIComponent(groupId)}`,
      { stage: 'mgmt_fetch_payout_group', expectedStatuses }),
    filterPayoutGroups: (searchValue) => call('GET', '/payouts-groups/filter',
      { query: { searchValue }, stage: 'mgmt_filter_payout_groups' }),
    removePayoutGroup: (groupId) => call('DELETE', `/payouts-groups/${encodeURIComponent(groupId)}`,
      { stage: 'mgmt_remove_payout_group' }),
    savePayoutGroupReferences: (groupId, references) => call('POST',
      `/payouts-groups/${encodeURIComponent(groupId)}/references`,
      { body: references, stage: 'mgmt_save_payout_group_references' }),
    removePayoutGroupReference: (groupId, partyId, walletId) => call('DELETE',
      `/payouts-groups/${encodeURIComponent(groupId)}/references`,
      { query: { partyId, walletId }, stage: 'mgmt_remove_payout_group_reference' }),
    filterPayoutGroupReferences: (query) => call('GET', '/payouts-groups/references/filter',
      { query, stage: 'mgmt_filter_payout_group_references' }),

    savePayoutListRows: (listType, records, expectedStatuses) => call('POST', '/payouts-lists',
      { body: { listType, records }, stage: 'mgmt_save_payout_list_rows', expectedStatuses }),
    removePayoutListRow: (id, expectedStatuses) => call('DELETE', `/payouts-lists/${encodeURIComponent(id)}`,
      { stage: 'mgmt_remove_payout_list_row', expectedStatuses }),
    filterPayoutLists: (query, expectedStatuses) => call('GET', '/payouts-lists/filter',
      { query, stage: 'mgmt_filter_payout_lists', expectedStatuses }),
    fetchAvailablePayoutListNames: () => call('GET', '/payouts-lists/availableListNames',
      { stage: 'mgmt_available_payout_list_names' }),
    fetchCurrentPayoutListNames: (listType) => call('GET', '/payouts-lists/currentListNames',
      { query: { listType }, stage: 'mgmt_current_payout_list_names' }),

    filterWithdrawalsHistory: (query, expectedStatuses) => call('GET', '/payouts-historical-data/withdrawals',
      { query, stage: 'mgmt_filter_withdrawals_history', expectedStatuses }),
  };
}
