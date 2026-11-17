import { cell, count, milliseconds, rate, readRun, stageLatency } from './results.mjs';

const directory = process.argv[2];
if (!directory) {
  console.error('Usage: report.mjs <results directory>');
  process.exit(2);
}
const run = readRun(directory);
const lines = [`# ${run.started.suite || 'k6'} / ${run.started.profile || '?'} / ${run.started.version || '?'}`, ''];
lines.push(`Run id: \`${run.started.run_id || '?'}\``, '');

if (run.cases.size || (run.started.cases || []).length) {
  const statuses = [...run.cases.values()].reduce((all, item) => ({ ...all, [item.status]: (all[item.status] || 0) + 1 }), {});
  const missing = (run.started.cases || []).filter((id) => !run.cases.has(id));
  lines.push(`Cases: ${Object.entries(statuses).map(([status, total]) => `${status} ${total}`).join(', ')}`
    + (missing.length ? `, not finished ${missing.length} (${missing.join(', ')})` : ''), '');
  lines.push('| Case | Status | Seconds | Details |', '| --- | --- | --- | --- |');
  [...run.cases.values()].sort((left, right) => left.case_id.localeCompare(right.case_id)).forEach((item) => {
    const observations = Object.entries(item.observations || {}).map(([key, value]) => `${key}=${value}`).join('; ');
    const details = [item.error, observations].filter(Boolean).join(' — ');
    lines.push(`| ${item.case_id} | ${item.status} | ${(item.duration_ms / 1000).toFixed(1)} | ${cell(details)} |`);
  });
  lines.push('');
}

const stages = stageLatency(run.metrics);
if (stages.length) {
  lines.push(`Traffic: iterations ${count(run.metrics, 'iterations') ?? '—'}, flow success `
    + `${rate(run.metrics, 'flow_success') ?? '—'}, checks ${rate(run.metrics, 'checks') ?? '—'}, `
    + `dropped iterations ${count(run.metrics, 'dropped_iterations') ?? 0}, errors ${run.trafficErrors.length}`, '');
  lines.push('| Stage | Requests | avg ms | p90 ms | p95 ms | p99 ms | max ms |', '| --- | --- | --- | --- | --- | --- | --- |');
  const reconfiguration = run.metrics.reconfiguration_time;
  if (reconfiguration && reconfiguration.values) {
    lines.splice(lines.length - 2, 0, `Template change under load (NF-07): applied in `
      + `${milliseconds(reconfiguration.values.max)} ms, success ${rate(run.metrics, 'reconfiguration_success')}`, '');
  }
  stages.forEach((item) => lines.push(`| ${item.stage} | ${item.count ?? '—'} | ${milliseconds(item.avg)} | `
    + `${milliseconds(item['p(90)'])} | ${milliseconds(item['p(95)'])} | ${milliseconds(item['p(99)'])} | ${milliseconds(item.max)} |`));
  lines.push('');
}
console.log(lines.join('\n'));
