import { existsSync, readFileSync } from 'node:fs';
import { join } from 'node:path';

export function readRun(directory) {
  const log = existsSync(join(directory, 'run.log')) ? readFileSync(join(directory, 'run.log'), 'utf8') : '';
  const events = log.split('\n')
    .filter((line) => line.startsWith('{'))
    .map((line) => {
      try {
        return JSON.parse(line);
      } catch {
        return undefined;
      }
    })
    .filter(Boolean);
  const started = events.find((event) => event.event === 'run_started') || {};
  const cases = new Map(events.filter((event) => event.event === 'case_finished').map((event) => [event.case_id, event]));
  const summaryPath = join(directory, 'summary.json');
  const summary = existsSync(summaryPath) ? JSON.parse(readFileSync(summaryPath, 'utf8')) : { metrics: {} };
  return { directory, started, cases, metrics: summary.metrics || {}, trafficErrors: events.filter((e) => e.event === 'traffic_error') };
}

export function stageLatency(metrics) {
  return Object.entries(metrics)
    .map(([name, metric]) => [name.match(/^http_req_duration\{scenario:traffic,stage:([^}]+)\}$/), metric])
    .filter(([match]) => match)
    .map(([match, metric]) => ({ stage: match[1], ...metric.values }));
}

export function rate(metrics, name) {
  return metrics[name] && metrics[name].values ? metrics[name].values.rate : undefined;
}

export function count(metrics, name) {
  return metrics[name] && metrics[name].values ? metrics[name].values.count : undefined;
}

export function milliseconds(value) {
  return value === undefined ? '—' : `${value.toFixed(1)}`;
}

export function cell(value) {
  return String(value === undefined || value === null ? '' : value).replace(/\|/g, '\\|').replace(/\n/g, ' ');
}
