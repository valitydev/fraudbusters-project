export function fail(message) {
  throw new Error(message);
}

export function expectTrue(condition, message) {
  if (!condition) fail(message);
}

export function expectEqual(actual, expected, what) {
  if (JSON.stringify(actual) !== JSON.stringify(expected)) {
    fail(`${what}: expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`);
  }
}

export function expectSuccess(response, what) {
  if (response.status < 200 || response.status > 299) {
    fail(`${what}: HTTP ${response.status} from ${response.url}: ${truncate(response.body)}`);
  }
  return response.json();
}

export function expectStatus(response, statuses, what) {
  if (!statuses.includes(response.status)) {
    fail(`${what}: expected HTTP ${statuses.join('|')}, got ${response.status} from ${response.url}: `
      + truncate(response.body));
  }
  return response.json();
}

export function sameMembers(actual, expected, what) {
  expectEqual([...actual].sort(), [...expected].sort(), what);
}

export function expectOutcomes(context, outcomes) {
  const mismatches = outcomes
    .map(({ name, actual, expected }) => {
      context.observe(name, actual);
      return expected === undefined || JSON.stringify(actual) === JSON.stringify(expected)
        ? undefined
        : `${name}: ${JSON.stringify(actual)}, expected ${JSON.stringify(expected)}`;
    })
    .filter(Boolean);
  expectTrue(mismatches.length === 0, mismatches.join('; '));
}

export function expectFatalOnceReached(decisions, what) {
  const firstFatal = decisions.indexOf('fatal');
  expectTrue(firstFatal >= 0, `${what}: never reached fatal, decisions ${decisions.join(',')}`);
  expectTrue(decisions.slice(firstFatal).every((decision) => decision === 'fatal'),
    `${what}: fatal is not stable, decisions ${decisions.join(',')}`);
}

export function validationErrors(body) {
  return (body.validateResults || []).flatMap((item) => item.errors || []);
}

function truncate(value) {
  const textValue = value === null || value === undefined ? '' : String(value);
  return textValue.length > 300 ? `${textValue.slice(0, 300)}…` : textValue;
}
