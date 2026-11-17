import { cell, milliseconds, readRun, stageLatency } from './results.mjs';

const [referenceDirectory, candidateDirectory] = process.argv.slice(2);
if (!referenceDirectory || !candidateDirectory) {
  console.error('Usage: compare.mjs <reference results> <candidate results>');
  process.exit(2);
}
const tolerance = Number(process.env.TOLERANCE_PERCENT || 20);
const reference = readRun(referenceDirectory);
const candidate = readRun(candidateDirectory);
const regressions = [];
const lines = [`# ${reference.started.version || '?'} → ${candidate.started.version || '?'}`, ''];

const caseIds = [...new Set([...reference.cases.keys(), ...candidate.cases.keys()])].sort();
if (caseIds.length) {
  lines.push('| Case | Reference | Candidate | Differences |', '| --- | --- | --- | --- |');
  caseIds.forEach((id) => {
    const left = reference.cases.get(id);
    const right = candidate.cases.get(id);
    const differences = [];
    const leftObservations = (left && left.observations) || {};
    const rightObservations = (right && right.observations) || {};
    [...new Set([...Object.keys(leftObservations), ...Object.keys(rightObservations)])].forEach((key) => {
      if (JSON.stringify(leftObservations[key]) !== JSON.stringify(rightObservations[key])) {
        differences.push(`${key}: ${leftObservations[key]} → ${rightObservations[key]}`);
      }
    });
    if (right && right.status === 'failed' && right.error) differences.push(right.error);
    const leftStatus = left ? left.status : 'absent';
    const rightStatus = right ? right.status : 'absent';
    if (leftStatus === 'passed' && !['passed', 'skipped'].includes(rightStatus)) {
      regressions.push(`${id}: passed → ${rightStatus}`);
    }
    if (right && right.status === 'skipped' && right.error) differences.push(right.error);
    lines.push(`| ${id} | ${leftStatus} | ${rightStatus} | ${cell(differences.join('; '))} |`);
  });
  lines.push('');
}

const leftStages = new Map(stageLatency(reference.metrics).map((item) => [item.stage, item]));
const rightStages = stageLatency(candidate.metrics);
if (rightStages.length) {
  lines.push(`p95 tolerance: +${tolerance}%`, '', '| Stage | Reference p95 ms | Candidate p95 ms | Change |',
    '| --- | --- | --- | --- |');
  rightStages.forEach((item) => {
    const before = leftStages.get(item.stage);
    const left = before && before['p(95)'];
    const right = item['p(95)'];
    const change = left ? ((right - left) / left) * 100 : undefined;
    if (change !== undefined && change > tolerance) {
      regressions.push(`${item.stage}: p95 ${milliseconds(left)} → ${milliseconds(right)} ms (+${change.toFixed(1)}%)`);
    }
    lines.push(`| ${item.stage} | ${milliseconds(left)} | ${milliseconds(right)} | `
      + `${change === undefined ? '—' : `${change > 0 ? '+' : ''}${change.toFixed(1)}%`} |`);
  });
  lines.push('');
}

lines.push(regressions.length ? `Regressions:\n${regressions.map((item) => `- ${item}`).join('\n')}` : 'No regressions');
console.log(lines.join('\n'));
process.exit(regressions.length ? 1 : 0);
