export default {
  withdrawals: 12,
  succeeded: 8,
  failedErrorCode: 'k6_error',
  providerCountries: ['RUS', 'KAZ'],
  pageSize: 5,
  filters: ['walletId', 'withdrawalId', 'status', 'email', 'cardToken', 'fingerprint', 'terminal', 'providerCountry',
    'userId', 'customerId'],
  changing: ['pending', 'succeeded'],
};
