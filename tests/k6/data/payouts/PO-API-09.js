export default {
  payouts: 12,
  succeeded: 8,
  failedErrorCode: 'k6_error',
  providerCountries: ['RUS', 'KAZ'],
  pageSize: 5,
  filters: ['walletId', 'payoutId', 'status', 'email', 'cardToken', 'fingerprint', 'terminal', 'providerCountry',
    'userId', 'customerId'],
  changing: ['pending', 'succeeded'],
};
