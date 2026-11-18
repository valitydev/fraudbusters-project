import { expectSuccess } from '../../lib/assert.js';
import { waitFor } from '../../lib/poll.js';
import { historyWindow } from '../../lib/time.js';
import { buildChargeback, buildRefund } from '../../fixtures/payment.js';
import { paymentSteps } from './steps.js';

export const REFUNDS = {
  build: buildRefund,
  load: (context, operations) => context.inspection.loadRefunds(operations),
  history: (context, partyId) => context.management.filterRefundsHistory({ ...historyWindow(), partyId, size: 100 }),
};

export const CHARGEBACKS = {
  build: buildChargeback,
  load: (context, operations) => context.inspection.loadChargebacks(operations),
  history: (context, partyId) => context.management.filterChargebacksHistory(
    { ...historyWindow(), partyId, size: 100 }),
};

export function awaitOperationsInHistory(context, kind, partyId, operationIds) {
  return waitFor(context.connection, `${operationIds.length} operations in history`, () => {
    const body = expectSuccess(kind.history(context, partyId), 'filter operation history');
    const found = (body.result || []).filter((item) => operationIds.some((id) => Object.values(item).includes(id)));
    return { done: found.length === operationIds.length, observed: found.length };
  });
}

export function expectOperationChangesDecision(context, kind, data) {
  const steps = paymentSteps(context);
  const variants = steps.applyTemplates(data.variants.map((variant) => ({
    ...variant,
    scope: { ...steps.scope(variant.role), cardToken: context.ids.name(`card-${variant.role}`) },
  })));
  variants.forEach((variant) => {
    const payment = steps.payment(variant.scope);
    steps.loadAndAwait([payment]);
    const operation = kind.build(payment, context.ids.unique('operation'));
    expectSuccess(kind.load(context, [operation]), 'load operation');
    awaitOperationsInHistory(context, kind, variant.scope.partyId, [operation.id]);
    steps.awaitDecision(`${variant.role} over the card`, () => steps.payment(variant.scope), data.expected);
  });
}
