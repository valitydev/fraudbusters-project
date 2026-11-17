import { Rate, Trend } from 'k6/metrics';
import { scopedIds } from './ids.js';

export const caseSuccess = new Rate('case_success');
const caseDuration = new Trend('case_duration', true);

export function runCase(definition, context) {
  const startedAt = Date.now();
  const observations = {};
  const caseContext = {
    ...context,
    ids: scopedIds(context.runId, definition.id),
    observe(key, value) {
      observations[key] = value;
    },
  };
  let status = 'passed';
  let error;
  if (definition.skip) {
    status = 'skipped';
    error = definition.skip;
  } else {
    try {
      definition.run(caseContext);
    } catch (exception) {
      status = 'failed';
      error = exception && exception.message ? exception.message : String(exception);
    }
    caseSuccess.add(status === 'passed', { case_id: definition.id });
    caseDuration.add(Date.now() - startedAt, { case_id: definition.id });
  }
  console.log(JSON.stringify({
    event: 'case_finished',
    suite: context.suite,
    version: context.version,
    run_id: context.runId,
    case_id: definition.id,
    title: definition.title,
    status,
    error,
    observations,
    duration_ms: Date.now() - startedAt,
  }));
}
