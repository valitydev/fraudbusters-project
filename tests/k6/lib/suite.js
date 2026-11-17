import execution from 'k6/execution';
import { connection, text } from './env.js';
import { runCase } from './cases.js';
import { randomRunId } from './ids.js';
import { expectSuccess } from './assert.js';
import { executionOptions, selectedCases, splitCases, summaryFiles } from './profiles.js';
import { managementClient } from '../clients/management.js';
import { inspectionClient } from '../clients/inspection.js';
import { payoutManagementClient } from '../clients/payouts.js';
import { payoutInspectorClient } from '../clients/payout-inspector.js';

export function clients() {
  const target = connection();
  return {
    connection: target,
    management: managementClient(target),
    inspection: inspectionClient(target),
    payouts: payoutManagementClient(target),
    payoutInspector: payoutInspectorClient(target),
  };
}

export function defineSuite({ name, cases, traffic, runId = () => __ENV.RUN_ID || randomRunId(), profile }) {
  const version = text('VERSION', 'unknown');
  const selected = selectedCases(cases);
  const { parallel, sequential } = splitCases(selected);
  return {
    options: executionOptions(name, selected, { trafficProfiles: Boolean(traffic) }),

    setup() {
      const context = clients();
      expectSuccess(context.management.filterPaymentTemplates({ size: 1 }), 'management is reachable');
      const id = runId();
      console.log(JSON.stringify({
        event: 'run_started', suite: name, run_id: id, version, profile: profile || text('K6_PROFILE', 'functional'),
        cases: selected.map((item) => item.id),
      }));
      const prepared = !selected.length && traffic ? traffic.prepare({ ...context, runId: id }) : undefined;
      return { runId: id, traffic: prepared };
    },

    runCases(data) {
      const definition = (execution.scenario.name === 'global_cases' ? sequential : parallel)[
        execution.scenario.iterationInTest];
      runCase(definition, { ...clients(), suite: name, runId: data.runId, version });
    },

    runTraffic(data) {
      traffic.iterate({ ...clients(), runId: data.runId }, data.traffic);
    },

    runReconfigure(data) {
      traffic.reconfigure({ ...clients(), runId: data.runId }, data.traffic);
    },

    handleSummary(data) {
      return summaryFiles(name, data);
    },
  };
}
