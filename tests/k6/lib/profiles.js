import { integer, list, text } from './env.js';

const CASE_PROFILES = ['functional', 'smoke'];
const LOAD_PROFILES = ['baseline', 'load', 'stress', 'spike', 'soak'];

export function profileName() {
  const profile = text('K6_PROFILE', 'functional');
  if (![...CASE_PROFILES, ...LOAD_PROFILES].includes(profile)) {
    throw new Error(`Unknown K6_PROFILE ${profile}; expected ${[...CASE_PROFILES, ...LOAD_PROFILES].join(', ')}`);
  }
  return profile;
}

export function selectedCases(allCases) {
  const profile = profileName();
  const requested = list('K6_CASES');
  if (requested.length) {
    const unknown = requested.filter((id) => !allCases.some((item) => item.id === id));
    if (unknown.length) throw new Error(`Unknown K6_CASES: ${unknown.join(', ')}`);
    return allCases.filter((item) => requested.includes(item.id));
  }
  if (!CASE_PROFILES.includes(profile)) return [];
  return allCases.filter((item) => item.profiles.includes(profile));
}

function trafficScenario(profile) {
  const rate = integer('RATE', 10, 1);
  const common = {
    exec: 'runTraffic',
    timeUnit: '1s',
    preAllocatedVUs: integer('VUS', 50, 1),
    maxVUs: integer('MAX_VUS', 200, 1),
    gracefulStop: '30s',
  };
  switch (profile) {
    case 'baseline':
      return { ...common, executor: 'constant-arrival-rate', rate, duration: text('DURATION', '5m') };
    case 'soak':
      return { ...common, executor: 'constant-arrival-rate', rate, duration: text('DURATION', '1h') };
    case 'load':
      return {
        ...common, executor: 'ramping-arrival-rate', startRate: 1,
        stages: [
          { target: integer('TARGET_RATE', rate * 2, 1), duration: text('RAMP_DURATION', '1m') },
          { target: integer('TARGET_RATE', rate * 2, 1), duration: text('DURATION', '5m') },
          { target: 0, duration: '30s' },
        ],
      };
    case 'stress': {
      const step = text('STEP_DURATION', '2m');
      return {
        ...common, executor: 'ramping-arrival-rate', startRate: rate,
        stages: list('STRESS_FACTORS', '1,2,3,4').map(Number).flatMap((factor) => [
          { target: rate * factor, duration: '20s' }, { target: rate * factor, duration: step }]),
      };
    }
    case 'spike':
      return {
        ...common, executor: 'ramping-arrival-rate', startRate: rate,
        stages: [
          { target: rate, duration: '1m' },
          { target: integer('SPIKE_RATE', rate * 4, 1), duration: '30s' },
          { target: integer('SPIKE_RATE', rate * 4, 1), duration: '2m' },
          { target: rate, duration: '30s' },
          { target: rate, duration: '2m' },
        ],
      };
    default:
      return undefined;
  }
}

export const COMPARED_STAGES = ['api_inspect_payment', 'api_load_payments', 'mgmt_filter_payments_history',
  'thrift_inspect_withdrawal', 'api_load_withdrawals'];

export function withGlobalCasesPermission(cases) {
  if (text('K6_GLOBAL_CASES', 'off') === 'on') return cases;
  return cases.map((item) => (item.global && !item.skip
    ? { ...item, skip: 'global cases change the whole system: set K6_GLOBAL_CASES=on' } : item));
}

export function splitCases(cases) {
  return { parallel: cases.filter((item) => !item.global), sequential: cases.filter((item) => item.global) };
}

export function executionOptions(suite, cases, { trafficProfiles = true } = {}) {
  const profile = profileName();
  const scenarios = {};
  const thresholds = {};
  const { parallel, sequential } = splitCases(cases);
  const maxDuration = text('CASES_MAX_DURATION', '40m');
  if (parallel.length) {
    scenarios.cases = {
      executor: 'shared-iterations',
      exec: 'runCases',
      vus: Math.min(integer('CONCURRENCY', 6, 1), parallel.length),
      iterations: parallel.length,
      maxDuration,
    };
  }
  if (sequential.length) {
    scenarios.global_cases = {
      executor: 'per-vu-iterations', exec: 'runCases', vus: 1, iterations: sequential.length, maxDuration,
    };
  }
  cases.filter((item) => !item.skip).forEach((item) => {
    thresholds[`case_success{case_id:${item.id}}`] = ['rate==1'];
  });
  const traffic = trafficProfiles ? trafficScenario(profile) : undefined;
  if (traffic) {
    scenarios.traffic = traffic;
    if (profile === 'load' && text('RECONFIGURE', 'on') !== 'off') {
      scenarios.reconfigure = {
        executor: 'per-vu-iterations', exec: 'runReconfigure', vus: 1, iterations: 1,
        startTime: text('RECONFIGURE_AT', '3m'), maxDuration: '5m',
      };
      thresholds.reconfiguration_success = ['rate==1'];
    }
    thresholds.flow_success = ['rate==1'];
    thresholds.flows_finished = ['count>0'];
    thresholds.checks = ['rate==1'];
    thresholds.dropped_iterations = ['count==0'];
    thresholds['http_req_failed{scenario:traffic}'] = ['rate==0'];
    COMPARED_STAGES.forEach((stage) => {
      thresholds[`http_req_duration{scenario:traffic,stage:${stage}}`] = [`p(95)<${integer('P95_LIMIT_MS', 60000, 1)}`];
    });
  }
  if (!Object.keys(scenarios).length) throw new Error(`Profile ${profile} selects nothing to run`);
  return {
    scenarios,
    thresholds,
    setupTimeout: '10m',
    tags: { suite, profile, version: text('VERSION', 'unknown') },
    summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max', 'count'],
  };
}

export function summaryFiles(suite, data) {
  const directory = text('RESULTS_DIR', 'results');
  const failed = Object.entries(data.metrics)
    .filter(([, metric]) => metric.thresholds && Object.values(metric.thresholds).some((item) => !item.ok))
    .map(([name]) => name);
  const lines = [
    `suite=${suite} profile=${profileName()} version=${text('VERSION', 'unknown')}`,
    failed.length ? `FAILED thresholds:\n  ${failed.join('\n  ')}` : 'All thresholds passed',
  ];
  return {
    stdout: `\n${lines.join('\n')}\n`,
    [`${directory}/summary.json`]: JSON.stringify(data, null, 2),
  };
}
