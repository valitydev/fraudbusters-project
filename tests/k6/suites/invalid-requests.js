import { expectTrue } from '../lib/assert.js';
import { ANY_STATUS } from '../lib/http.js';

export function expectNoServerErrors(context, client, { requests, knownServerErrors = [] }) {
  const serverErrors = Object.entries(requests)
    .map(([name, { search, query }]) => {
      const { status } = client[search](query, ANY_STATUS);
      context.observe(name, status);
      return status >= 500 && !knownServerErrors.includes(name) ? `${name}: HTTP ${status}` : undefined;
    })
    .filter(Boolean);
  expectTrue(serverErrors.length === 0, `server errors on invalid parameters: ${serverErrors.join('; ')}`);
}
