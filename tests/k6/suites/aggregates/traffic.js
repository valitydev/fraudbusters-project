import { check } from 'k6';
import execution from 'k6/execution';
import { Counter, Rate, Trend } from 'k6/metrics';
import data from '../../data/aggregates/traffic.js';
import { expectSuccess } from '../../lib/assert.js';
import { text } from '../../lib/env.js';
import { scopedIds } from '../../lib/ids.js';
import { waitFor } from '../../lib/poll.js';
import { historyWindow } from '../../lib/time.js';
import { paymentChange } from '../../fixtures/payment.js';
import { paymentSteps } from '../payments/steps.js';

const flowSuccess = new Rate('flow_success');
const flowsFinished = new Counter('flows_finished');
const reconfigurationSuccess = new Rate('reconfiguration_success');
const reconfigurationTime = new Trend('reconfiguration_time', true);

const pad = (value) => String(value).padStart(2, '0');
// Same formula as seed.sql: a stable pseudo-random share of a card for its home party, shop, email, fingerprint.
const mix = (card, salt) => ((card * 2654435761 + salt) % 4294967296) / 4294967296;
const below = (count) => Math.floor(Math.random() * count);

function seededShop(party, shop) {
  return { partyId: `seed-party-${pad(party)}`, shopId: `seed-shop-${pad(party)}-${pad(shop)}` };
}

function allSeededShops(shape) {
  return Array.from({ length: shape.parties }, (unused, party) => Array.from({ length: shape.shops },
    (unusedShop, shop) => seededShop(party, shop))).flat();
}

// A payment of the seeded population: hot cards are picked more often, mostly at their home shop.
function seededPayment(shape) {
  const card = Math.floor(Math.random() ** 3 * shape.cards);
  const home = Math.random() < shape.homeShop;
  const party = home ? Math.floor(mix(card, 0) ** 2 * shape.parties) : below(shape.parties);
  const shop = home ? Math.floor(mix(card, 7) * shape.shops) : below(shape.shops);
  const email = Math.random() < shape.homeEmail ? card % shape.emails : below(shape.emails);
  const fingerprint = Math.random() < shape.homeFingerprint ? card % shape.fingerprints : below(shape.fingerprints);
  const ip = 167772160 + Math.floor(Math.random() ** 2 * shape.ips);
  return {
    ...seededShop(party, shop),
    cardToken: `seed-card-${card}`,
    email: `seed-${email}@seed.test`,
    fingerprint: `seed-fp-${fingerprint}`,
    ip: [ip >>> 24, (ip >>> 16) & 255, (ip >>> 8) & 255, ip & 255].join('.'),
    amount: Math.round(Math.exp(Math.log(100) + Math.random() * (Math.log(300000) - Math.log(100)))),
  };
}

function weights() {
  return Object.entries(data.shares)
    .map(([flow, share]) => [flow, Number(text(`SHARE_${flow.toUpperCase()}`, share))])
    .filter(([, share]) => share > 0);
}

function pickFlow() {
  const shares = weights();
  let point = Math.random() * shares.reduce((sum, [, share]) => sum + share, 0);
  const picked = shares.find(([, share]) => {
    point -= share;
    return point < 0;
  });
  return (picked || shares[0])[0];
}

function stepsOf(context) {
  const ids = scopedIds(context.runId, 'aggregates');
  return { ids, payments: paymentSteps({ ...context, ids }) };
}

// Fresh identities per payment, so that probes and checks do not share email or fingerprint by accident.
function fresh(ids, key) {
  return {
    id: ids.name(`payment-${key}`),
    cardToken: ids.name(`card-${key}`),
    email: `${ids.name(`email-${key}`)}@k6.test`,
    fingerprint: ids.name(`fingerprint-${key}`),
  };
}

function iterationKey() {
  return `${execution.vu.idInTest}-${execution.vu.iterationInScenario}`;
}

function prepare(context, shape) {
  const { ids, payments } = stepsOf(context);
  const shops = allSeededShops(shape);
  const probeShop = shops[0];
  const templateId = payments.saveTemplate(ids.name('template'), data.template);
  const bound = expectSuccess(context.management.savePaymentReferences(shops.map((shop) => ({ templateId, ...shop }))),
    'bind template to seeded shops');
  if (!Array.isArray(bound.result) || bound.result.length !== shops.length) {
    throw new Error(`bind template: unexpected answer ${JSON.stringify(bound)}`);
  }
  [shops[0], shops[shops.length - 1]].forEach((shop, index) => payments.awaitDecision(`template applied ${index}`,
    () => payments.payment({ ...shop, ...fresh(ids, `ready-${index}-${Date.now()}`) }), 'low'));

  const probes = {};
  Object.entries(data.probes).forEach(([name, probe]) => {
    const card = ids.name(`${name}-card`);
    const identity = { ...probeShop, cardToken: card, email: `${ids.name(`${name}-email`)}@k6.test`,
      fingerprint: ids.name(`${name}-fingerprint`) };
    if (name === 'black') payments.addListRow('black', 'CARD_TOKEN', card, probeShop);
    if (probe.history) {
      const history = Array.from({ length: probe.history.count }, (unused, index) => payments.payment({
        ...identity, id: ids.name(`${name}-history-${index}`),
        ...(probe.history.distinctEmails ? { email: `${ids.name(`${name}-email-${index}`)}@k6.test` } : {}),
      }));
      payments.load(history, probe.history.status, probe.history.errorCode);
    }
    payments.awaitDecision(`${name} probe`, () => payments.payment({ ...identity, id: ids.unique(`${name}-check`) }),
      probe.decision);
    probes[name] = identity;
  });
  return { templateId, shops, probeShop, probes };
}

function loadedChange(payment, status, errorCode) {
  return paymentChange(payment, status, errorCode ? { errorCode, errorMessage: 'k6' } : undefined);
}

function inspectFlow(context, target, shape, name) {
  const flow = data.flows[name];
  const { ids, payments } = stepsOf(context);
  const key = iterationKey();
  const tags = { flow: name };
  let payment;
  let expected;
  let load;
  if (flow.probe) {
    const probe = data.probes[flow.probe];
    payment = payments.payment({ ...target.probes[flow.probe], id: ids.name(`payment-${key}`) });
    expected = [probe.decision];
    if (probe.refill) load = { status: probe.refill, errorCode: probe.history.errorCode };
  } else if (name === 'seeded') {
    payment = payments.payment({ ...seededPayment(shape), id: ids.name(`payment-${key}`) });
    expected = flow.decisions;
    const failed = Math.random() < data.seededLoad.failedShare;
    load = failed ? { status: 'failed', errorCode: data.errorCodes[below(data.errorCodes.length)] }
      : { status: 'captured' };
  } else {
    const shop = target.shops[below(target.shops.length)];
    payment = payments.payment({ ...shop, ...fresh(ids, key), ...(flow.amount ? { amount: flow.amount } : {}) });
    expected = [flow.decision];
    if (flow.loaded) load = { status: 'captured' };
  }
  const response = context.inspection.inspectPayment(payment);
  const decision = response.status === 200 ? response.json().result : `http_${response.status}`;
  const decided = check(decision, { [`${name} decision`]: (value) => expected.includes(value) }, tags);
  if (!decided) {
    console.warn(JSON.stringify({ event: 'unexpected_decision', flow: name, decision, expected,
      payment: payment.id, card: payment.paymentResource.cardToken, party: payment.merchant.id }));
  }
  if (!load) return decided;
  const loaded = context.inspection.loadPaymentChanges([loadedChange(payment, load.status, load.errorCode)]);
  return check(loaded, { 'payment loaded': (value) => value.status === 201 }, tags) && decided;
}

function iterate(context, target, shape) {
  const name = pickFlow();
  let success = false;
  try {
    success = inspectFlow(context, target, shape, name);
    if (Math.random() < Number(text('SHARE_HISTORY_SEARCH', data.historySearchShare))) {
      const shop = target.shops[below(target.shops.length)];
      const body = expectSuccess(context.management.filterPaymentsHistory(
        { ...historyWindow(), partyId: shop.partyId, size: 10 }), 'history search');
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
  const { ids, payments } = stepsOf(context);
  const startedAt = Date.now();
  let success = false;
  try {
    payments.saveTemplate(target.templateId, `${data.reconfiguration.rule}\n${data.template}`);
    waitFor(context.connection, 'changed template under load', () => {
      const decision = payments.inspect(payments.payment({ ...target.probeShop,
        ...fresh(ids, `reconfigure-${Date.now()}`), amount: data.reconfiguration.amount }));
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

export function aggregatesTraffic(shape) {
  return {
    prepare: (context) => prepare(context, shape),
    iterate: (context, target) => iterate(context, target, shape),
    reconfigure,
  };
}
