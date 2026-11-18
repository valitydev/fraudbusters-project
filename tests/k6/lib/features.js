export const KNOWN_FEATURES = ['payouts'];

export function featuresOf(name) {
  const value = __ENV[name];
  if (value === undefined) return KNOWN_FEATURES;
  const features = value.split(',').map((item) => item.trim()).filter((item) => item !== '');
  const unknown = features.filter((item) => !KNOWN_FEATURES.includes(item));
  if (unknown.length) {
    throw new Error(`${name} has unknown features ${unknown.join(', ')}; known: ${KNOWN_FEATURES.join(', ')}`);
  }
  return features;
}

export function unavailabilityReason({ since, until }, features) {
  if (since && !features.includes(since)) return `needs ${since}`;
  if (until && features.includes(until)) return `replaced in ${until}`;
  return undefined;
}

export function withAvailability(cases, features, since) {
  return cases.map((item) => {
    const reason = item.skip || unavailabilityReason({ since: item.since || since, until: item.until }, features);
    return reason ? { ...item, skip: reason } : item;
  });
}
