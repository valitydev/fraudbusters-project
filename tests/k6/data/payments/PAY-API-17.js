const BLOCKED_BY_ANTIFRAUD = 'no_route_found:risk_score_is_too_high';

export default {
  history: [
    { count: 3, status: 'captured' },
    { count: 2, status: 'failed', with: { amount: 150000 }, errorCode: BLOCKED_BY_ANTIFRAUD },
    { count: 1, status: 'failed', with: { amount: 70000 }, errorCode: 'insufficient_funds' },
  ],
  expected: {
    blockedCount: 2,
    blockedRatio: 2 / 6,
    blockedSumVariants: [300000, 3000],
  },
  answered: ['fraud-payments/count', 'fraud-payments/results/summary'],
  splitUnit: 'hour',
  currency: 'RUB',
};
