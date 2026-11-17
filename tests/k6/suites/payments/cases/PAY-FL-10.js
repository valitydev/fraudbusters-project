import data from '../../../data/payments/PAY-FL-10.js';
import { CHARGEBACKS, expectOperationChangesDecision } from '../operations.js';

export default {
  id: 'PAY-FL-10',
  title: 'countChargeback, sumChargeback',
  profiles: ['functional'],
  run(context) {
    expectOperationChangesDecision(context, CHARGEBACKS, data);
  },
};
