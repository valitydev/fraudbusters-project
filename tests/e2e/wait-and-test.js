const http = require('http');
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
(async () => {
    if (urls.some((url) => !url)) throw new Error('FB_MNGMT_URL and FB_API_URL are required');
    while (!(await Promise.all(urls.map(reachable))).every(Boolean)) {
        if (Date.now() >= deadline) throw new Error('Timed out waiting for application HTTP endpoints');
        await new Promise((resolve) => setTimeout(resolve, 2000));
    }
    const result = spawnSync('npm', ['test'], { stdio: 'inherit' });
    process.exit(result.status === null ? 1 : result.status);
})().catch((error) => {
    console.error(error.message);
    process.exit(1);
});
