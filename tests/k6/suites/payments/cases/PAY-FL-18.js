import data from '../../../data/payments/PAY-FL-18.js';
import { expectStoredDecisions } from '../scenarios.js';

export default {
  id: 'PAY-FL-18',
  title: 'isRecurrent, isMobile',
  profiles: ['functional'],
  run(context) {
    expectStoredDecisions(context, 'type', data);
  },
};
