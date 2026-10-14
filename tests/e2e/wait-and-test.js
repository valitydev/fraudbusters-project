const http = require('http');
const net = require('net');
const { spawnSync } = require('child_process');

const urls = [process.env.FB_MNGMT_URL, process.env.FB_API_URL];
const deadline = Date.now() + Number(process.env.READINESS_TIMEOUT_MS || 300000);
function reachable(url) {
    return new Promise((resolve) => {
        const request = http.get(url, (response) => {
            response.resume();
            resolve(true);
        });
        request.setTimeout(3000, () => request.destroy());
        request.on('error', () => resolve(false));
    });
}
function coreReachable() {
    return new Promise((resolve) => {
        const socket = net.createConnection({ host: process.env.FB_CORE_HOST || 'fraudbusters', port: 8022 });
        socket.setTimeout(3000);
        socket.once('connect', () => {
            socket.destroy();
            resolve(true);
        });
        socket.once('timeout', () => socket.destroy());
        socket.once('error', () => resolve(false));
        socket.once('close', () => resolve(false));
    });
}
(async () => {
    if (urls.some((url) => !url)) throw new Error('FB_MNGMT_URL and FB_API_URL are required');
    while (!(await Promise.all([...urls.map(reachable), coreReachable()])).every(Boolean)) {
        if (Date.now() >= deadline) throw new Error('Timed out waiting for application HTTP endpoints');
        await new Promise((resolve) => setTimeout(resolve, 2000));
    }
    const result = spawnSync('npm', ['test'], { stdio: 'inherit' });
    process.exit(result.status === null ? 1 : result.status);
})().catch((error) => {
    console.error(error.message);
    process.exit(1);
});
