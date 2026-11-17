import data from '../../../data/payments/PAY-API-17.js';
import { expectEqual, expectSuccess, expectTrue } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';
import { waitFor } from '../../../lib/poll.js';
import { historyWindow } from '../../../lib/time.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-API-17',
  title: 'Аналитика',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { management, connection } = context;
    const scope = steps.scope();
    data.history.forEach((entry) => steps.loadAndAwait(Array.from({ length: entry.count }, () => steps.payment(
      { ...scope, ...entry.with })), entry.status, entry.errorCode));
    const window = historyWindow();
    const query = { fromTime: window.from, toTime: window.to, currency: data.currency, merchantId: scope.partyId,
      shopId: scope.shopId };
    const fetch = (name, extra = {}) => {
      const response = management.fetchAnalytics(name, { ...query, ...extra }, ANY_STATUS);
      return { status: response.status, body: response.json() };
    };
    const { expected } = data;
    const count = waitFor(connection, 'blocked payments are counted', () => {
      const answer = fetch('fraud-payments/blocked/count');
      return { done: answer.status === 200 && answer.body && answer.body.count === expected.blockedCount,
        value: answer.body, observed: JSON.stringify(answer) };
    });
    expectEqual(count.count, expected.blockedCount, 'blocked payments count');
    const ratio = fetch('fraud-payments/blocked/count/ratio');
    context.observe('blocked_ratio', JSON.stringify(ratio.body));
    expectTrue(ratio.status === 200 && Math.abs(ratio.body.ratio - expected.blockedRatio) < 0.01,
      `blocked ratio: ${JSON.stringify(ratio)}`);
    const sum = fetch('fraud-payments/blocked/sum');
    context.observe('blocked_sum', JSON.stringify(sum.body));
    expectTrue(sum.status === 200 && expected.blockedSumVariants.includes(Math.round(sum.body.sum)),
      `blocked sum: ${JSON.stringify(sum)}`);
    data.answered.forEach((name) => {
      const answer = fetch(name);
      context.observe(name, `${answer.status}:${JSON.stringify(answer.body).slice(0, 200)}`);
      expectTrue(answer.status === 200, `${name}: HTTP ${answer.status}`);
    });
    const split = fetch('fraud-payments/scores/split-count/ratio', { splitUnit: data.splitUnit });
    context.observe('split_status', split.status);
    expectTrue(split.status === 200 && split.body && split.body.splitUnit === data.splitUnit,
      `split count ratio: ${JSON.stringify(split).slice(0, 300)}`);
    const currencies = expectSuccess(management.fetchAnalytics('currencies', {}), 'currencies');
    expectTrue(JSON.stringify(currencies).includes(data.currency),
      `currencies: ${JSON.stringify(currencies).slice(0, 200)}`);
  },
};
