import { expectSuccess } from '../lib/assert.js';
import { waitFor } from '../lib/poll.js';
import { historyWindow } from '../lib/time.js';

export function awaitAuditRecords(context, { commandTypes, objectTypes, expectedRecords }, marker) {
  return waitFor(context.connection, 'audit records of the case', () => {
    const found = (expectSuccess(context.management.filterAudit({
      ...historyWindow(), commandTypes, objectTypes, sortOrder: 'DESC', size: 500,
    }), 'filter audit').result || []).filter((item) => String(item.object || '').includes(marker));
    const kinds = new Set(found.map((item) => `${item.objectType}:${item.commandType}`));
    const done = expectedRecords.every((kind) => kinds.has(kind));
    return { done, value: found, observed: [...kinds].sort().join(',') };
  });
}
