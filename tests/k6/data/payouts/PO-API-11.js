export default {
  requests: {
    lists_unknown_list_type: { search: 'filterPayoutLists', query: { listType: 'unknown', listNames: ['EMAIL'], size: 10 } },
    templates_unknown_sort_order: { search: 'filterPayoutTemplates', query: { sortOrder: 'FOO', size: 10 } },
    history_without_window: { search: 'filterWithdrawalsHistory', query: { size: 10 } },
    templates_size_zero: { search: 'filterPayoutTemplates', query: { size: 0 } },
    templates_size_negative: { search: 'filterPayoutTemplates', query: { size: -1 } },
  },
};
