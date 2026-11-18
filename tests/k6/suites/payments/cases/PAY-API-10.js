import data from '../../../data/payments/PAY-API-10.js';
import { expectSuccess } from '../../../lib/assert.js';
import { waitFor } from '../../../lib/poll.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-API-10',
  title: 'Списки: загрузка и удаление CSV',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { management, ids, connection } = context;
    const { partyId, shopId } = steps.scope();
    const { black, grey } = ids.resolve(data);
    const values = ids.numbered('csv-card', black.rows);
    const csv = [black.header, ...values.map((value) => `${partyId},${shopId},${black.listName},${value}`)].join('\n');
    expectSuccess(management.uploadPaymentListCsv('black', csv), 'upload black list CSV');
    const rows = () => steps.findListRows('black', black.listName, partyId).length;
    waitFor(connection, `${black.rows} rows from CSV`, () => ({ done: rows() === black.rows, observed: rows() }));
    const now = Date.now();
    const greyCsv = [grey.header, [partyId, shopId, grey.listName, grey.value, grey.count, new Date(now).toISOString(),
      new Date(now + grey.durationMs).toISOString()].join(',')].join('\n');
    expectSuccess(management.uploadPaymentListCsv('grey', greyCsv), 'upload grey list CSV');
    steps.awaitListRow('grey', grey.listName, grey.value);
    expectSuccess(management.removePaymentListCsv('black', csv), 'remove black list CSV');
    waitFor(connection, 'rows removed by CSV', () => ({ done: rows() === 0, observed: rows() }));
  },
};
