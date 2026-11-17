import { sleep } from 'k6';

export function waitFor(connection, description, attempt, timeoutMs = connection.waitTimeoutMs) {
  const deadline = Date.now() + timeoutMs;
  let observed = 'nothing';
  for (;;) {
    const result = attempt();
    if (result.done) return result.value;
    observed = result.observed === undefined ? observed : result.observed;
    const remaining = deadline - Date.now();
    if (remaining <= 0) {
      throw new Error(`Timeout after ${timeoutMs} ms waiting for ${description}; last observed: ${observed}`);
    }
    sleep(Math.min(connection.pollIntervalMs, remaining) / 1000);
  }
}

export function waitForCount(connection, description, search, expected) {
  return waitFor(connection, `${description}: ${expected} records`, () => {
    const body = search();
    const size = (body.result || []).length;
    return { done: size === expected, value: body, observed: size };
  });
}
