export function randomRunId() {
  return Math.random().toString(36).slice(2, 8);
}

export function scopedIds(runId, caseId) {
  const prefix = `k6-${runId}-${caseId.toLowerCase()}`;
  let sequence = 0;
  const ids = {
    prefix,
    name(role) {
      return `${prefix}-${role}`;
    },
    unique(role) {
      sequence += 1;
      return `${prefix}-${role}-${sequence}-${Math.random().toString(36).slice(2, 6)}`;
    },
    numbered(role, count) {
      return Array.from({ length: count }, (unused, index) => `${prefix}-${role}-${String(index).padStart(2, '0')}`);
    },
    resolve(value) {
      if (typeof value === 'string') {
        return value.replace(/\{\{([a-z0-9-]+)\}\}/g, (match, role) => (role === 'prefix' ? prefix : ids.name(role)));
      }
      if (Array.isArray(value)) return value.map(ids.resolve);
      if (value && typeof value === 'object') {
        return Object.fromEntries(Object.entries(value).map(([key, item]) => [key, ids.resolve(item)]));
      }
      return value;
    },
  };
  return ids;
}
