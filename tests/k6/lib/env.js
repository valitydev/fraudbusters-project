export function text(name, fallback) {
  const value = __ENV[name];
  return value === undefined || value === '' ? fallback : value;
}

export function integer(name, fallback, minimum = 0) {
  const value = Number(text(name, fallback));
  if (!Number.isSafeInteger(value) || value < minimum) {
    throw new Error(`${name} must be an integer >= ${minimum}`);
  }
  return value;
}

export function list(name, fallback = '') {
  return text(name, fallback)
    .split(',')
    .map((value) => value.trim())
    .filter((value) => value !== '');
}

export function baseUrl(name, fallback) {
  const value = text(name, fallback).replace(/\/$/, '');
  if (!/^https?:\/\/\S+$/.test(value)) throw new Error(`${name} must be an HTTP(S) URL`);
  return value;
}

export function connection() {
  return {
    managementUrl: baseUrl('FB_MANAGEMENT_URL', 'http://fb-management:8080/fb-management/v1'),
    apiUrl: baseUrl('FB_API_URL', 'http://fraudbusters-api:8080'),
    inspectorUrl: baseUrl('FB_INSPECTOR_URL', 'http://fraudbusters:8022/fraud_inspector/v1'),
    requestTimeout: `${integer('REQUEST_TIMEOUT_MS', 30000, 1)}ms`,
    waitTimeoutMs: integer('WAIT_TIMEOUT_MS', 90000, 1),
    pollIntervalMs: integer('POLL_INTERVAL_MS', 2000, 100),
  };
}
