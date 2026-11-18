import data from '../../../data/payments/PAY-FL-17.js';
import { expectStoredDecisions } from '../scenarios.js';

export default {
  id: 'PAY-FL-17',
  title: 'Гео: countryBy по ip и стране банка',
  profiles: ['functional'],
  run(context) {
    expectStoredDecisions(context, 'geo', data);
  },
};
