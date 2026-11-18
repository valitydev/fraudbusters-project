export default {
  white: { name: 'EMAIL', value: '{{prefix}}-white@k6.test' },
  grey: { name: 'CARD_TOKEN', value: '{{grey}}', countInfo: { count: 5, fromMs: 0, toMs: 3600 * 1000 } },
  naming: { name: '{{list}}', value: '{{named-value}}' },
  black: { name: 'CARD_TOKEN', rows: 5, pageSize: 2 },
};
