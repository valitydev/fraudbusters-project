export default {
  payments: 3,
  fraudPaymentsByCsv: 2,
  fraud: { type: 'k6-fraud', comment: '{{prefix}}' },
  csvHeader: 'id,event_time,fraud_type,comment',
};
