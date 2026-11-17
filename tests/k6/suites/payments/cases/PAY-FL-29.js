import data from '../../../data/payments/PAY-FL-29.js';
import { expectOutcomes } from '../../../lib/assert.js';
import { storedDecisionOutcomes } from '../scenarios.js';

export default {
  id: 'PAY-FL-29',
  title: 'Функции и операторы: userId, paymentSystem, cardCategory, rand, like, not, or, !=, дроби',
  profiles: ['functional'],
  run(context) {
    expectOutcomes(context, [
      ...storedDecisionOutcomes(context, 'functions', data.functions),
      ...storedDecisionOutcomes(context, 'precedence', data.precedence),
    ]);
  },
};
