import http from 'k6/http';
import { check } from 'k6';
import { buildPayment } from '../fixtures/payment.js';
import { authorizationHeaders } from '../lib/auth.js';
import { connection } from '../lib/env.js';
import { historyWindow } from '../lib/time.js';

export const options = { iterations: 1, thresholds: { checks: ['rate==1'] } };

export default function () {
  const target = connection();
  const headers = { 'Content-Type': 'application/json', ...authorizationHeaders() };
  const management = http.get(`${target.managementUrl}/payments-templates/filter?size=1`, { headers, timeout: '10s' });
  const payment = buildPayment({ id: 'k6-ready', partyId: 'k6-ready', shopId: 'k6-ready', cardToken: 'k6-ready' });
  const inspection = http.post(`${target.apiUrl}/inspect-payment`, JSON.stringify({ payment }),
    { headers, timeout: '10s' });
  const window = historyWindow();
  const history = http.get(`${target.managementUrl}/payments-historical-data/payments-info?size=1`
    + `&from=${encodeURIComponent(window.from)}&to=${encodeURIComponent(window.to)}`, { headers, timeout: '10s' });
  check(management, { 'management is ready': (response) => response.status === 200 });
  check(inspection, { 'inspection is ready': (response) => response.status === 200 });
  check(history, { 'history in ClickHouse is ready': (response) => response.status === 200 });
}
