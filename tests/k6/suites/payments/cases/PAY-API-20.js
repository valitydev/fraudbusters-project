import data from '../../../data/payments/PAY-API-20.js';
import { expectOutcomes } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';

export default {
  id: 'PAY-API-20',
  title: 'Загрузка данных в fraudbusters-api',
  profiles: ['functional'],
  run(context) {
    const { inspection, ids } = context;
    expectOutcomes(context, [
      ...data.emptyLoads.map((load) => ({
        name: `empty_${load}`, actual: inspection[load]([], ANY_STATUS).status, expected: data.expected.empty,
      })),
      ...Object.entries(data.invalidRecords).map(([name, { load, record }]) => ({
        name, actual: inspection[load]([ids.resolve(record)], ANY_STATUS).status, expected: data.expected.invalid,
      })),
    ]);
  },
};
