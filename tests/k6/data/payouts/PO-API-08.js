export default {
  availableListNames: ['EMAIL', 'IP', 'FINGERPRINT', 'USER_ID', 'CARD_TOKEN', 'BIN', 'LAST_DIGITS', 'MASKED_PAN', 'PHONE',
    'COUNTRY_BANK', 'CURRENCY', 'PARTY_ID', 'WALLET_ID'],
  white: { name: 'EMAIL', value: '{{prefix}}-white@k6.test' },
  grey: { name: 'CARD_TOKEN', value: '{{grey}}', countInfo: { count: 5, fromMs: 0, toMs: 3600 * 1000 } },
  naming: { name: '{{list}}', value: '{{named}}' },
  invalidRows: {
    unknown_list_name: { listName: 'SHOP_ID', value: 'x' },
    empty_value: { listName: 'EMAIL', value: '' },
    wallet_without_party: { listName: 'EMAIL', value: 'x@k6.test', walletId: '{{wallet}}' },
  },
  black: { name: 'CARD_TOKEN', rows: 5, pageSize: 2 },
  expected: { invalidRow: 400, removeMissing: 404 },
};
