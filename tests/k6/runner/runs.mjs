import { spawn } from 'node:child_process';
import { randomBytes } from 'node:crypto';
import { createWriteStream, existsSync, mkdirSync, readdirSync, readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

export const PROFILES = ['smoke', 'functional', 'baseline', 'load', 'stress', 'spike', 'soak'];

export const SETTINGS = ['K6_CASES', 'RUN_ID', 'RATE', 'TARGET_RATE', 'SPIKE_RATE', 'DURATION', 'RAMP_DURATION',
  'STEP_DURATION', 'VUS', 'MAX_VUS', 'CONCURRENCY', 'WAIT_TIMEOUT_MS', 'POLL_INTERVAL_MS', 'REQUEST_TIMEOUT_MS',
  'CASES_MAX_DURATION', 'P95_LIMIT_MS', 'SHARE_ACCEPT', 'SHARE_AMOUNT', 'SHARE_BLACK', 'SHARE_VELOCITY',
  'SHARE_HISTORY_SEARCH', 'SHARE_PAYOUT_ACCEPT', 'SHARE_PAYOUT_BLACK', 'RECONFIGURE', 'RECONFIGURE_AT',
  'STRESS_FACTORS', 'K6_PHASE', 'FEATURES', 'FROM_FEATURES', 'FB_MANAGEMENT_URL', 'FB_API_URL', 'FB_INSPECTOR_URL',
  'FB_AUTH_URL', 'FB_AUTH_REALM', 'FB_AUTH_CLIENT', 'READY_TIMEOUT', 'K6_GLOBAL_CASES'];

export const SECRETS = ['FB_TOKEN', 'FB_AUTH_USER', 'FB_AUTH_PASSWORD'];

export const RUN_FILES = {
  'run.log': 'text/plain; charset=utf-8',
  'ready.log': 'text/plain; charset=utf-8',
  'report.md': 'text/markdown; charset=utf-8',
  'summary.json': 'application/json',
  'run.json': 'application/json',
};

const FINISHED_STATES = ['passed', 'failed', 'cancelled', 'interrupted'];
const READY_RETRY_MS = 10000;
const CANCEL_GRACE_MS = 90000;

export class RequestError extends Error {
  constructor(status, message, details = {}) {
    super(message);
    this.status = status;
    this.details = details;
  }
}

function utcStamp(date) {
  return date.toISOString().replace(/[-:]/g, '').replace(/\.\d+Z$/, 'Z');
}

function validStringMap(value, allowed, field) {
  if (value === undefined) return {};
  if (typeof value !== 'object' || value === null || Array.isArray(value)) {
    throw new RequestError(400, `${field} must be an object`);
  }
  const unknown = Object.keys(value).filter((key) => !allowed.includes(key));
  if (unknown.length) throw new RequestError(400, `${field} has unknown keys: ${unknown.join(', ')}`);
  return Object.fromEntries(Object.entries(value).map(([key, item]) => {
    if (!['string', 'number', 'boolean'].includes(typeof item)) {
      throw new RequestError(400, `${field}.${key} must be a string, number or boolean`);
    }
    return [key, String(item)];
  }));
}

export function createRunner({
  testsDirectory, resultsDirectory, k6Binary = 'k6', nodeBinary = process.execPath, baseEnvironment = process.env,
  readyRetryMs = READY_RETRY_MS, cancelGraceMs = CANCEL_GRACE_MS,
}) {
  mkdirSync(resultsDirectory, { recursive: true });
  const runs = new Map();
  let active;

  function runDirectory(id) {
    return join(resultsDirectory, id);
  }

  function saveRun(run) {
    writeFileSync(join(runDirectory(run.id), 'run.json'), `${JSON.stringify(run, null, 2)}\n`);
  }

  function updateRun(run, changes) {
    Object.assign(run, changes);
    saveRun(run);
  }

  function loadSavedRuns() {
    readdirSync(resultsDirectory, { withFileTypes: true })
      .filter((entry) => entry.isDirectory() && existsSync(join(resultsDirectory, entry.name, 'run.json')))
      .forEach((entry) => {
        const run = JSON.parse(readFileSync(join(resultsDirectory, entry.name, 'run.json'), 'utf8'));
        if (!FINISHED_STATES.includes(run.state)) {
          Object.assign(run, { state: 'interrupted', finishedAt: new Date().toISOString(),
            error: 'the runner restarted during the run' });
          saveRun(run);
        }
        runs.set(run.id, run);
      });
  }

  function suites() {
    return readdirSync(join(testsDirectory, 'suites'), { withFileTypes: true })
      .filter((entry) => entry.isDirectory() && existsSync(join(testsDirectory, 'suites', entry.name, 'main.js')))
      .map((entry) => entry.name)
      .sort();
  }

  function parseRunRequest(body) {
    if (typeof body !== 'object' || body === null || Array.isArray(body)) {
      throw new RequestError(400, 'the request body must be a JSON object');
    }
    const { suite, profile, version } = body;
    if (typeof suite !== 'string' || !suites().includes(suite)) {
      throw new RequestError(400, `suite must be one of: ${suites().join(', ')}`);
    }
    if (typeof profile !== 'string' || !PROFILES.includes(profile)) {
      throw new RequestError(400, `profile must be one of: ${PROFILES.join(', ')}`);
    }
    if (version !== undefined && (typeof version !== 'string' || !/^[\w.-]{1,64}$/.test(version))) {
      throw new RequestError(400, 'version must be up to 64 letters, digits, dots, dashes or underscores');
    }
    return {
      suite,
      profile,
      version: version || baseEnvironment.VERSION || 'unknown',
      settings: validStringMap(body.settings, SETTINGS, 'settings'),
      secrets: validStringMap(body.secrets, SECRETS, 'secrets'),
    };
  }

  function processEnvironment(run, secrets) {
    return {
      ...baseEnvironment,
      ...run.settings,
      ...secrets,
      K6_PROFILE: run.profile,
      VERSION: run.version,
      RESULTS_DIR: runDirectory(run.id),
    };
  }

  function runProcess(command, args, environment, logPath, register) {
    return new Promise((resolve) => {
      const log = createWriteStream(logPath, { flags: 'a' });
      const child = spawn(command, args, { cwd: testsDirectory, env: environment, stdio: ['ignore', 'pipe', 'pipe'] });
      register(child);
      child.stdout.pipe(log, { end: false });
      child.stderr.pipe(log, { end: false });
      child.on('error', (error) => {
        log.end(`${error.message}\n`);
        resolve(127);
      });
      child.on('close', (code, signal) => {
        log.end();
        resolve(code ?? (signal ? 128 : 1));
      });
    });
  }

  function sleep(milliseconds) {
    return new Promise((resolve) => { setTimeout(resolve, milliseconds); });
  }

  async function waitUntilReady(run, environment, control) {
    const timeoutMs = Number(environment.READY_TIMEOUT || 600) * 1000;
    const deadline = Date.now() + timeoutMs;
    for (;;) {
      const code = await runProcess(k6Binary, ['run', '--quiet', '--log-format=raw', join(testsDirectory, 'tools/ready.js')],
        environment, join(runDirectory(run.id), 'ready.log'), (child) => { control.child = child; });
      control.child = undefined;
      if (code === 0) return true;
      if (control.cancelled || Date.now() + readyRetryMs > deadline) return false;
      await sleep(readyRetryMs);
      if (control.cancelled) return false;
    }
  }

  async function writeReport(run) {
    const directory = runDirectory(run.id);
    const reportPath = join(directory, 'report.md');
    const report = createWriteStream(reportPath);
    const child = spawn(nodeBinary, [join(testsDirectory, 'tools/report.mjs'), directory],
      { cwd: testsDirectory, stdio: ['ignore', 'pipe', 'ignore'] });
    child.stdout.pipe(report);
    await new Promise((resolve) => { child.on('close', resolve); child.on('error', resolve); });
  }

  async function execute(run, secrets, control) {
    const environment = processEnvironment(run, secrets);
    const ready = await waitUntilReady(run, environment, control);
    if (control.cancelled) {
      updateRun(run, { state: 'cancelled', finishedAt: new Date().toISOString() });
      return;
    }
    if (!ready) {
      updateRun(run, { state: 'failed', finishedAt: new Date().toISOString(),
        error: 'the environment is not ready: management, inspection or history do not answer, see ready.log' });
      return;
    }
    updateRun(run, { state: 'running', startedAt: new Date().toISOString() });
    const exitCode = await runProcess(k6Binary,
      ['run', '--quiet', '--log-format=raw', join(testsDirectory, 'suites', run.suite, 'main.js')],
      environment, join(runDirectory(run.id), 'run.log'), (child) => { control.child = child; });
    control.child = undefined;
    await writeReport(run);
    updateRun(run, {
      state: control.cancelled ? 'cancelled' : (exitCode === 0 ? 'passed' : 'failed'),
      exitCode,
      finishedAt: new Date().toISOString(),
    });
  }

  function start(body) {
    if (active) throw new RequestError(409, `run ${active.run.id} is in progress`, { active: active.run.id });
    const request = parseRunRequest(body);
    const createdAt = new Date();
    const id = `${request.suite}-${request.profile}-${utcStamp(createdAt)}-${randomBytes(2).toString('hex')}`;
    const run = {
      id,
      suite: request.suite,
      profile: request.profile,
      version: request.version,
      settings: request.settings,
      credentials: Object.keys(request.secrets),
      state: 'waiting',
      createdAt: createdAt.toISOString(),
    };
    mkdirSync(runDirectory(id), { recursive: true });
    saveRun(run);
    runs.set(id, run);
    const control = { cancelled: false, child: undefined };
    active = { run, control };
    active.done = execute(run, request.secrets, control)
      .catch((error) => updateRun(run, { state: 'failed', finishedAt: new Date().toISOString(), error: error.message }))
      .finally(() => { active = undefined; });
    return run;
  }

  function find(id) {
    const run = runs.get(id);
    if (!run) throw new RequestError(404, `run ${id} is not found`);
    return run;
  }

  function cancel(id) {
    const run = find(id);
    if (!active || active.run.id !== id) throw new RequestError(409, `run ${id} is not in progress`);
    active.control.cancelled = true;
    const { child } = active.control;
    if (child) {
      child.kill('SIGINT');
      setTimeout(() => {
        if (child.exitCode === null && child.signalCode === null) child.kill('SIGKILL');
      }, cancelGraceMs).unref();
    }
    return run;
  }

  function filePath(id, name) {
    find(id);
    if (!RUN_FILES[name]) throw new RequestError(404, `file ${name} is not served; files: ${Object.keys(RUN_FILES).join(', ')}`);
    const path = join(runDirectory(id), name);
    if (!existsSync(path)) throw new RequestError(404, `run ${id} has no ${name} yet`);
    return path;
  }

  function list() {
    return [...runs.values()].sort((left, right) => right.createdAt.localeCompare(left.createdAt));
  }

  function activeCompletion() {
    return active ? active.done : Promise.resolve();
  }

  loadSavedRuns();

  return {
    suites, start, find, cancel, filePath, list, runDirectory, activeCompletion,
    activeRunId: () => (active ? active.run.id : null),
  };
}
