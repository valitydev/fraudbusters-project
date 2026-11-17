import data from '../../../data/payments/PAY-FL-27.js';
import { expectOutcomes } from '../../../lib/assert.js';
import { REST_RESULT, paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-27',
  title: 'Шаблон в форме боевого: списки, like, доверие, pending, коды ошибок',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { ids } = context;
    const scope = steps.scope();
    const payment = ({ client, with: overrides = {} }) => steps.payment({
      ...scope, email: `${ids.prefix}-${client}@k6.test`, fingerprint: ids.name(`fingerprint-${client}`), amount: 1000,
      ...overrides,
    });
    steps.bindNewTemplate('template', data.template, scope);
    steps.awaitDecision('template is applied', () => payment(data.ready), data.ready.decision);
    data.lists.forEach((row) => steps.addListRow(row.type, row.name, ids.resolve(row.value), scope));
    data.listChecks.forEach((check) => steps.awaitDecision(`${check.client} list`, () => payment(check), check.decision));
    data.history.forEach((entry) => steps.loadAndAwait(Array.from({ length: entry.count }, () => payment(entry)),
      entry.status, entry.errorCode));

    const outcomes = data.scenarios.flatMap((scenario) => {
      const { rest, status, rule } = steps.inspectAndReadStored(payment(scenario));
      if (scenario.observe) {
        return [{ name: scenario.name, actual: scenario.observe === 'rest' ? rest : `${status}/${rule}` }];
      }
      const decision = { name: scenario.name, actual: `${status}/${rule}`, expected: scenario.expected };
      return REST_RESULT[status]
        ? [decision, { name: `${scenario.name} (REST)`, actual: rest, expected: REST_RESULT[status] }]
        : [decision];
    });
    const spelling = outcomes.find((outcome) => outcome.name === 'code_spelling_decision');
    outcomes.push({ name: 'code_without_space_counted_by_rule_with_space', actual: spelling.actual.startsWith('decline') });
    expectOutcomes(context, outcomes);
  },
};
