import data from '../../../data/payouts/PO-API-11.js';
import { expectNoServerErrors } from '../../invalid-requests.js';

export default {
  id: 'PO-API-11',
  title: 'Некорректные параметры API выплат',
  profiles: ['functional'],
  run(context) {
    expectNoServerErrors(context, context.payouts, data);
  },
};
