export function historyTime(date) {
  return date.toISOString().slice(0, 19).replace('T', ' ');
}

export function historyWindow() {
  const now = Date.now();
  return {
    from: historyTime(new Date(now - 24 * 3600 * 1000)),
    to: historyTime(new Date(now + 24 * 3600 * 1000)),
  };
}

export function countInfoFor(count, fromMs, toMs) {
  const now = Date.now();
  return {
    count,
    startCountTime: new Date(now + fromMs).toISOString(),
    endCountTime: new Date(now + toMs).toISOString(),
  };
}
