import data from '../../../data/payments/PAY-API-19.js';
import { expectNoServerErrors } from '../../invalid-requests.js';

export default {
  id: 'PAY-API-19',
  title: 'Некорректные параметры management',
  profiles: ['functional'],
  run(context) {
    expectNoServerErrors(context, context.management, data);
  },
};
