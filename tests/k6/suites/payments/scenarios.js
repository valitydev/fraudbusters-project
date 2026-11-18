import { expectOutcomes } from '../../lib/assert.js';
import { paymentSteps } from './steps.js';

export function storedDecisionOutcomes(context, role, { template, ready, scenarios }) {
  const steps = paymentSteps(context);
  const scope = steps.scope(role);
  steps.applyTemplate(`template-${role}`, template, scope, ready);
  return steps.storedDecisionOutcomes(scenarios.map((scenario) => ({
    ...scenario, payment: steps.payment({ ...scope, ...scenario.payment }),
  })));
}

export function expectStoredDecisions(context, role, data) {
  expectOutcomes(context, storedDecisionOutcomes(context, role, data));
}
