export default {
  requests: {
    history_without_window: { search: 'filterPaymentsHistory', query: { size: 10 } },
    templates_unknown_sort_order: { search: 'filterPaymentTemplates', query: { sortOrder: 'FOO', size: 10 } },
    lists_unknown_list_type: { search: 'filterPaymentLists', query: { listType: 'unknown', listNames: ['EMAIL'], size: 10 } },
    templates_size_zero: { search: 'filterPaymentTemplates', query: { size: 0 } },
    templates_size_negative: { search: 'filterPaymentTemplates', query: { size: -1 } },
    templates_without_size: { search: 'filterPaymentTemplates', query: {} },
  },
};
