import data from '../../../data/payments/PAY-FL-09.js';
import { expectOperationChangesDecision, REFUNDS } from '../operations.js';

export default {
  id: 'PAY-FL-09',
  title: 'countRefund, sumRefund',
  profiles: ['functional'],
  run(context) {
    expectOperationChangesDecision(context, REFUNDS, data);
  },
};
