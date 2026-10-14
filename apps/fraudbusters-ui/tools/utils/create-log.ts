export function createLog(prefix: string) {
    // eslint-disable-next-line no-console
    return (message: string) => console.log(`[${prefix}]: ${message}`);
}
