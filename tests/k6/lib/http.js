import http from 'k6/http';
import { authorizationHeaders } from './auth.js';

export const ANY_STATUS = [{ min: 100, max: 599 }];

export function send(connection, method, url, { body, query, stage, expectedStatuses } = {}) {
  const target = query ? `${url}?${encodeQuery(query)}` : url;
  const params = {
    headers: { 'Content-Type': 'application/json', Accept: 'application/json', ...authorizationHeaders() },
    timeout: connection.requestTimeout,
    tags: { stage, name: stage },
    responseCallback: http.expectedStatuses(...(expectedStatuses || [{ min: 200, max: 299 }])),
  };
  const payload = body === undefined ? null : JSON.stringify(body);
  const response = http.request(method, target, payload, params);
  return {
    status: response.status,
    body: response.body,
    url: `${method} ${url}`,
    json() {
      if (!response.body) return undefined;
      try {
        return JSON.parse(response.body);
      } catch (error) {
        return undefined;
      }
    },
  };
}

function encodeQuery(query) {
  return Object.entries(query)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .flatMap(([key, value]) => (Array.isArray(value) ? value : [value])
      .map((item) => `${encodeURIComponent(key)}=${encodeURIComponent(item)}`))
    .join('&');
}
