import assert from 'node:assert/strict';
import { chmodSync, mkdtempSync, readdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { after, beforeEach, describe, test } from 'node:test';
import { fileURLToPath } from 'node:url';
import { createRunner } from './runs.mjs';
import { createRunnerServer } from './server.mjs';

const testsDirectory = join(dirname(fileURLToPath(import.meta.url)), '..');
const scratch = mkdtempSync(join(tmpdir(), 'k6-runner-test-'));
const fakeK6 = join(scratch, 'fake-k6');

writeFileSync(fakeK6, `#!/bin/sh
case "$*" in
  *ready.js*) exit "\${FAKE_READY_EXIT:-0}" ;;
esac
echo '{"event":"run_started","suite":"payments","run_id":"fake","version":"'"$VERSION"'","profile":"'"$K6_PROFILE"'","cases":["PAY-FL-01"]}'
echo '{"event":"case_finished","case_id":"PAY-FL-01","title":"fake","status":"passed","observations":{},"duration_ms":1000}'
echo "environment FEATURES=$FEATURES GLOBAL=$K6_GLOBAL_CASES AUTH=\${FB_AUTH_USER:+set}"
echo '{"metrics":{}}' > "$RESULTS_DIR/summary.json"
if [ -n "$FAKE_SLEEP" ]; then exec sleep "$FAKE_SLEEP"; fi
exit "\${FAKE_EXIT:-0}"
`);
chmodSync(fakeK6, 0o755);

let resultsDirectory;

function runnerWith(environment = {}) {
  return createRunner({
    testsDirectory,
    resultsDirectory,
    k6Binary: fakeK6,
    baseEnvironment: { PATH: process.env.PATH, K6_GLOBAL_CASES: 'off', ...environment },
    readyRetryMs: 10,
  });
}

function filesOf(directory) {
  return readdirSync(directory, { recursive: true, withFileTypes: true })
    .filter((entry) => entry.isFile())
    .map((entry) => readFileSync(join(entry.parentPath ?? entry.path, entry.name), 'utf8'));
}

beforeEach(() => {
  resultsDirectory = mkdtempSync(join(scratch, 'results-'));
});

after(() => rmSync(scratch, { recursive: true, force: true }));

describe('run requests', () => {
  test('reject an unknown suite, profile, setting or secret', () => {
    const runner = runnerWith();
    const invalid = [
      { suite: 'missing', profile: 'smoke' },
      { suite: 'payments', profile: 'fast' },
      { suite: 'payments', profile: 'smoke', settings: { PATH: '/tmp' } },
      { suite: 'payments', profile: 'smoke', secrets: { FEATURES: 'payouts' } },
      { suite: 'payments', profile: 'smoke', settings: { RATE: { value: 1 } } },
      { suite: 'payments', profile: 'smoke', version: 'bad version' },
      ['payments'],
    ];
    invalid.forEach((body) => assert.throws(() => runner.start(body), { status: 400 }));
    assert.deepEqual(runner.list(), []);
  });

  test('run a suite with request settings over the pod environment', async () => {
    const runner = runnerWith({ FEATURES: 'payouts', VERSION: 'sha-pod' });
    const run = runner.start({
      suite: 'payments', profile: 'smoke', settings: { FEATURES: '', K6_GLOBAL_CASES: 'on' },
      secrets: { FB_AUTH_USER: 'officer', FB_AUTH_PASSWORD: 'not-for-disk' },
    });
    assert.equal(run.state, 'waiting');
    assert.equal(run.version, 'sha-pod');
    await runner.activeCompletion();
    const finished = runner.find(run.id);
    assert.equal(finished.state, 'passed');
    assert.equal(finished.exitCode, 0);
    assert.deepEqual(finished.credentials, ['FB_AUTH_USER', 'FB_AUTH_PASSWORD']);
    const log = readFileSync(runner.filePath(run.id, 'run.log'), 'utf8');
    assert.match(log, /environment FEATURES= GLOBAL=on AUTH=set/);
    assert.match(readFileSync(runner.filePath(run.id, 'report.md'), 'utf8'), /\| PAY-FL-01 \| passed \|/);
    assert.equal(filesOf(runner.runDirectory(run.id)).some((content) => content.includes('not-for-disk')), false);
  });

  test('allow one run at a time', async () => {
    const runner = runnerWith({ FAKE_SLEEP: '1' });
    const first = runner.start({ suite: 'payments', profile: 'smoke' });
    assert.throws(() => runner.start({ suite: 'payments', profile: 'smoke' }), { status: 409 });
    await runner.activeCompletion();
    assert.equal(runner.find(first.id).state, 'passed');
    runner.start({ suite: 'payments', profile: 'smoke' });
    await runner.activeCompletion();
  });

  test('fail a run when k6 fails', async () => {
    const runner = runnerWith({ FAKE_EXIT: '99' });
    const run = runner.start({ suite: 'payments', profile: 'smoke' });
    await runner.activeCompletion();
    assert.equal(runner.find(run.id).state, 'failed');
    assert.equal(runner.find(run.id).exitCode, 99);
  });

  test('fail a run when the environment never becomes ready', async () => {
    const runner = runnerWith({ FAKE_READY_EXIT: '1' });
    const run = runner.start({ suite: 'payments', profile: 'smoke', settings: { READY_TIMEOUT: '0.05' } });
    await runner.activeCompletion();
    const finished = runner.find(run.id);
    assert.equal(finished.state, 'failed');
    assert.match(finished.error, /not ready/);
    assert.throws(() => runner.filePath(run.id, 'run.log'), { status: 404 });
  });

  test('cancel a running suite', async () => {
    const runner = runnerWith({ FAKE_SLEEP: '30' });
    const run = runner.start({ suite: 'payments', profile: 'load' });
    while (runner.find(run.id).state !== 'running') await new Promise((resolve) => { setTimeout(resolve, 20); });
    runner.cancel(run.id);
    await runner.activeCompletion();
    assert.equal(runner.find(run.id).state, 'cancelled');
    assert.throws(() => runner.cancel(run.id), { status: 409 });
  });

  test('mark runs interrupted by a restart', async () => {
    const runner = runnerWith({ FAKE_SLEEP: '30' });
    const run = runner.start({ suite: 'payments', profile: 'smoke' });
    while (runner.find(run.id).state !== 'running') await new Promise((resolve) => { setTimeout(resolve, 20); });
    const restarted = runnerWith();
    assert.equal(restarted.find(run.id).state, 'interrupted');
    runner.cancel(run.id);
    await runner.activeCompletion();
  });
});

describe('HTTP API', () => {
  test('start a run and serve its results', async () => {
    const runner = runnerWith();
    const server = createRunnerServer(runner).listen(0);
    await new Promise((resolve) => { server.on('listening', resolve); });
    const base = `http://127.0.0.1:${server.address().port}`;
    try {
      assert.equal((await (await fetch(`${base}/health`)).json()).status, 'ok');
      assert.ok((await (await fetch(`${base}/suites`)).json()).suites.includes('payments'));
      const created = await fetch(`${base}/runs`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ suite: 'payments', profile: 'smoke', version: 'sha-1' }),
      });
      assert.equal(created.status, 202);
      const { id } = await created.json();
      await runner.activeCompletion();
      assert.equal((await (await fetch(`${base}/runs/${id}`)).json()).state, 'passed');
      assert.equal((await (await fetch(`${base}/runs`)).json()).runs[0].id, id);
      assert.match(await (await fetch(`${base}/runs/${id}/files/report.md`)).text(), /PAY-FL-01/);
      const archive = await fetch(`${base}/runs/${id}/archive`);
      assert.equal(archive.headers.get('content-type'), 'application/gzip');
      assert.deepEqual([...new Uint8Array(await archive.arrayBuffer()).slice(0, 2)], [0x1f, 0x8b]);
      assert.equal((await fetch(`${base}/runs/${id}/files/secrets.txt`)).status, 404);
      assert.equal((await fetch(`${base}/runs/missing`)).status, 404);
      assert.equal((await fetch(`${base}/runs`, { method: 'POST', body: '{' })).status, 400);
    } finally {
      server.close();
    }
  });
});
