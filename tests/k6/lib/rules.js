import { expectEqual, expectSuccess, expectTrue } from './assert.js';
import { waitFor } from './poll.js';

export const PROBE_AMOUNT = 2000000000;

function asLoad(load) {
  return typeof load === 'string' ? { status: load } : load;
}

export function ruleSteps(context, channel) {
  const { connection, ids } = context;

  const steps = {
    scope(role) {
      return channel.scope(role ? `-${role}` : '');
    },

    saveTemplate(templateId, rules) {
      const template = ids.resolve(rules).trim();
      const body = expectSuccess(channel.saveTemplate({ id: templateId, template }), `save template ${templateId}`);
      expectTrue(!body.errors || body.errors.length === 0,
        `template ${templateId} rejected: ${JSON.stringify(body.errors)}`);
      waitFor(connection, `template ${templateId} is stored`, () => {
        const found = channel.findTemplate(templateId);
        return { done: Boolean(found) && found.template === template, observed: found ? 'other text' : 'absent' };
      });
      return templateId;
    },

    bindTemplate(templateId, operationScope) {
      const scope = channel.referenceScope(operationScope);
      const body = expectSuccess(channel.saveReferences([{ templateId, ...scope }]), `bind template ${templateId}`);
      expectTrue(Array.isArray(body.result) && body.result.length === 1,
        `bind template ${templateId}: unexpected answer ${JSON.stringify(body)}`);
      waitFor(connection, `reference to ${templateId} is stored`, () => ({
        done: channel.findReferences(templateId, scope).some((reference) => channel.sameScope(reference, scope)),
      }));
      return body.result[0];
    },

    bindNewTemplate(role, template, scope) {
      const templateId = steps.saveTemplate(ids.name(role), template);
      steps.bindTemplate(templateId, scope);
      return templateId;
    },

    applyTemplate(role, template, scope, ready = {}) {
      const templateId = steps.bindNewTemplate(role, template, scope);
      steps.awaitReady(`${role} is applied`, scope, ready);
      return templateId;
    },

    applyTemplates(variants) {
      variants.forEach((variant) => {
        variant.templateId = steps.bindNewTemplate(`template-${variant.role}`, variant.template, variant.scope);
      });
      variants.forEach((variant) => steps.awaitReady(`${variant.role} is applied`, variant.scope, variant.ready));
      return variants;
    },

    awaitReady(description, scope, ready = {}) {
      const { decision = 'low', ...overrides } = ids.resolve(ready);
      return steps.awaitDecision(description, () => steps.operation({ ...scope, ...overrides }), decision);
    },

    saveGroup(groupId, priorities) {
      expectSuccess(channel.saveGroup({
        groupId, priorityTemplates: priorities.map(([id, priority]) => ({ id, priority })),
      }), `save group ${groupId}`);
      return groupId;
    },

    bindGroup(groupId, operationScope) {
      expectSuccess(channel.saveGroupReferences(groupId, [channel.referenceScope(operationScope)]),
        `bind group ${groupId}`);
    },

    operation(overrides) {
      return channel.build({
        id: ids.unique(channel.kind),
        email: `${ids.prefix}@k6.test`,
        fingerprint: ids.name('fingerprint'),
        cardToken: ids.name('card'),
        ...ids.resolve(overrides),
      });
    },

    inspect(operation) {
      return channel.inspect(operation);
    },

    awaitDecision(description, makeOperation, expected) {
      return waitFor(connection, `${description} = ${expected}`, () => {
        const operation = makeOperation();
        const decision = steps.inspect(operation);
        return { done: decision === expected, value: operation, observed: decision };
      });
    },

    expectDecision(operation, expected, what) {
      expectEqual(steps.inspect(operation), expected, what);
    },

    load(operations, status = channel.successStatus, errorCode = undefined) {
      expectSuccess(channel.load(operations, status, errorCode),
        `load ${operations.length} ${channel.kind}s with status ${status}`);
    },

    awaitHistory(operation, status = channel.successStatus) {
      return waitFor(connection, `${channel.kind} ${operation.id} with status ${status} in history`, () => {
        const items = channel.findHistory(operation);
        const found = items.find((item) => item.status === status);
        return { done: Boolean(found), value: found, observed: JSON.stringify(items.map((item) => item.status)) };
      });
    },

    loadAndAwait(operations, status = channel.successStatus, errorCode = undefined) {
      steps.load(operations, status, errorCode);
      return operations.map((operation) => steps.awaitHistory(operation, status));
    },

    loadEach(base, loads) {
      loads.map(asLoad).forEach((load) => steps.loadAndAwait([steps.operation({ ...base, ...load.with })],
        load.status, load.errorCode));
    },

    decisionsAfterEach(base, loads) {
      return loads.map(asLoad).map((load) => {
        steps.loadAndAwait([steps.operation({ ...base, ...load.with })], load.status, load.errorCode);
        return steps.inspect(steps.operation(base));
      });
    },

    addListRow(listType, listName, value, scope = {}, countInfo = undefined) {
      const body = expectSuccess(channel.saveListRows(listType, [{
        countInfo, listRecord: { listName, value, ...channel.referenceScope(scope) },
      }]),
        `add ${value} to ${listType} list ${listName}`);
      const errors = body && body.errors;
      expectTrue(!(errors && errors.length), `list row rejected: ${JSON.stringify(errors)}`);
      return body;
    },

    findListRows(listType, listName, searchValue) {
      return channel.findListRows(listType, listName, searchValue);
    },

    awaitListRow(listType, listName, value, scope = undefined) {
      return waitFor(connection, `${value} in ${listType} list ${listName}`, () => {
        const row = steps.findListRows(listType, listName, value)
          .find((item) => channel.rowValue(item) === value
            && (!scope || channel.sameScope(channel.rowScope(item), scope)));
        return { done: Boolean(row), value: row };
      });
    },
  };
  return steps;
}
