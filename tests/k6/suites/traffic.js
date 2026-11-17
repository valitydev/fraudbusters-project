import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import data from '../data/traffic.js';
import { expectSuccess } from '../lib/assert.js';
import { text } from '../lib/env.js';
import { scopedIds } from '../lib/ids.js';
import { waitFor } from '../lib/poll.js';
import { historyWindow } from '../lib/time.js';
import { paymentChange } from '../fixtures/payment.js';
import { withdrawalRecord } from '../fixtures/withdrawal.js';
import { paymentSteps } from './payments/steps.js';
import { payoutSteps } from './payouts/steps.js';

const flowSuccess = new Rate('flow_success');
const flowsFinished = new Counter('flows_finished');
const reconfigurationSuccess = new Rate('reconfiguration_success');
const reconfigurationTime = new Trend('reconfiguration_time', true);

function weights(mix) {
  return Object.entries(data.shares[mix])
    .map(([flow, share]) => [flow, Number(text(`SHARE_${flow.toUpperCase()}`, share))])
    .filter(([, share]) => share > 0);
}

function pickFlow(mix) {
  const shares = weights(mix);
  let point = Math.random() * shares.reduce((sum, [, share]) => sum + share, 0);
  const picked = shares.find(([, share]) => {
    point -= share;
    return point < 0;
  });
  return (picked || shares[0])[0];
}

function trafficSteps(context) {
  const ids = scopedIds(context.runId, 'traffic');
  return { ids, payments: paymentSteps({ ...context, ids }), payouts: payoutSteps({ ...context, ids }) };
}

function prepare(context, mix) {
  const { ids, payments, payouts } = trafficSteps(context);
  const target = {
    partyId: ids.name('party'), shopId: ids.name('shop'), walletId: ids.name('wallet'),
    cards: { black: ids.name('black-card'), velocity: ids.name('velocity-card') },
  };
  const shop = { partyId: target.partyId, shopId: target.shopId };
  target.templateId = payments.applyTemplate('template', data.paymentTemplate, shop, { cardToken: ids.unique('card') });
  payments.addListRow('black', 'CARD_TOKEN', target.cards.black, shop);
  payments.awaitDecision('black card', () => payments.payment({ ...shop, cardToken: target.cards.black }), 'fatal');
  payments.loadEach({ ...shop, cardToken: target.cards.velocity }, data.velocityHistory);
  payments.awaitDecision('velocity card', () => payments.payment({ ...shop, cardToken: target.cards.velocity }),
    'fatal');
  if (weights(mix).some(([flow]) => data.flows[flow].payout)) {
    const wallet = { partyId: target.partyId, walletId: target.walletId };
    payouts.bindNewTemplate('payout-template', data.payoutTemplate, { partyId: target.partyId });
    payouts.addListRow('black', 'CARD_TOKEN', target.cards.black, { partyId: target.partyId });
    payouts.awaitDecision('payout black card', () => payouts.withdrawal({ ...wallet, cardToken: target.cards.black }),
      'fatal');
    payouts.awaitReady('payout template is applied', wallet, { cardToken: ids.unique('card') });
  }
  return target;
}

function inspectFlow(context, target, name) {
  const flow = data.flows[name];
  const { ids, payments, payouts } = trafficSteps(context);
  const cardToken = flow.card ? target.cards[flow.card] : ids.unique('card');
  const tags = { flow: name };
  if (flow.payout) {
    const withdrawal = payouts.withdrawal({ partyId: target.partyId, walletId: target.walletId, cardToken });
    const answer = context.payoutInspector.inspectWithdrawal(withdrawal);
    const decided = check(answer.score || answer.failure, { [`${name} decision`]: (value) => value === flow.decision },
      tags);
    if (!flow.loaded) return decided;
    const loaded = context.inspection.loadWithdrawals([withdrawalRecord(withdrawal, 'succeeded')]);
    return check(loaded, { 'withdrawal loaded': (value) => value.status === 201 }, tags) && decided;
  }
  const payment = payments.payment({ partyId: target.partyId, shopId: target.shopId, cardToken, ...flow.with });
  const response = context.inspection.inspectPayment(payment);
  const decision = response.status === 200 ? response.json().result : `http_${response.status}`;
  const decided = check(decision, { [`${name} decision`]: (value) => value === flow.decision }, tags);
  if (!flow.loaded) return decided;
  const loaded = context.inspection.loadPaymentChanges([paymentChange(payment, 'captured')]);
  return check(loaded, { 'payment loaded': (value) => value.status === 201 }, tags) && decided;
}

function iterate(context, target, mix) {
  const name = pickFlow(mix);
  let success = false;
  try {
    success = inspectFlow(context, target, name);
    if (Math.random() < Number(text('SHARE_HISTORY_SEARCH', data.historySearchShare))) {
      const body = expectSuccess(context.management.filterPaymentsHistory(
        { ...historyWindow(), partyId: target.partyId, size: 10 }), 'history search');
      success = check(body, { 'history search': (value) => Array.isArray(value.result) }, { flow: name }) && success;
    }
  } catch (error) {
    console.error(JSON.stringify({ event: 'traffic_error', flow: name, error: error.message }));
    success = false;
  }
  flowsFinished.add(1, { flow: name });
  flowSuccess.add(success, { flow: name });
}

function reconfigure(context, target) {
  const { ids, payments } = trafficSteps(context);
  const startedAt = Date.now();
  let success = false;
  try {
    payments.saveTemplate(target.templateId, `${data.reconfiguration.rule}\n${data.paymentTemplate}`);
    waitFor(context.connection, 'changed template under load', () => {
      const decision = payments.inspect(payments.payment({ partyId: target.partyId, shopId: target.shopId,
        cardToken: ids.unique('card'), amount: data.reconfiguration.amount }));
      return { done: decision === data.reconfiguration.decision, observed: decision };
    });
    success = true;
  } catch (error) {
    console.error(JSON.stringify({ event: 'traffic_error', flow: 'reconfigure', error: error.message }));
  }
  reconfigurationTime.add(Date.now() - startedAt);
  reconfigurationSuccess.add(success);
  console.log(JSON.stringify({ event: 'reconfigured', success, duration_ms: Date.now() - startedAt }));
}

export function trafficOf(mix) {
  return {
    prepare: (context) => prepare(context, mix),
    iterate: (context, target) => iterate(context, target, mix),
    reconfigure,
  };
}
