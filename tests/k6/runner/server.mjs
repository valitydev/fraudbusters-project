import { spawn } from 'node:child_process';
import { createReadStream } from 'node:fs';
import { createServer } from 'node:http';
import { dirname, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { createRunner, PROFILES, RequestError, RUN_FILES } from './runs.mjs';

const MAX_BODY_BYTES = 64 * 1024;

function sendJson(response, status, body) {
  response.writeHead(status, { 'Content-Type': 'application/json' });
  response.end(`${JSON.stringify(body, null, 2)}\n`);
}

function readJsonBody(request) {
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    request.on('data', (chunk) => {
      size += chunk.length;
      if (size > MAX_BODY_BYTES) {
        reject(new RequestError(413, 'the request body is too large'));
        request.destroy();
        return;
      }
      chunks.push(chunk);
    });
    request.on('end', () => {
      try {
        resolve(chunks.length ? JSON.parse(Buffer.concat(chunks).toString('utf8')) : {});
      } catch {
        reject(new RequestError(400, 'the request body is not valid JSON'));
      }
    });
    request.on('error', reject);
  });
}

function streamArchive(response, directory, id) {
  response.writeHead(200, {
    'Content-Type': 'application/gzip',
    'Content-Disposition': `attachment; filename="${id}.tar.gz"`,
  });
  const archive = spawn('tar', ['czf', '-', '-C', directory, '.'], { stdio: ['ignore', 'pipe', 'ignore'] });
  archive.stdout.pipe(response);
}

export function createRunnerServer(runner) {
  return createServer(async (request, response) => {
    const url = new URL(request.url, 'http://runner');
    const parts = url.pathname.split('/').filter(Boolean);
    try {
      if (request.method === 'GET' && url.pathname === '/health') {
        sendJson(response, 200, { status: 'ok', active: runner.activeRunId() });
      } else if (request.method === 'GET' && url.pathname === '/suites') {
        sendJson(response, 200, { suites: runner.suites(), profiles: PROFILES });
      } else if (request.method === 'GET' && url.pathname === '/runs') {
        sendJson(response, 200, { runs: runner.list() });
      } else if (request.method === 'POST' && url.pathname === '/runs') {
        sendJson(response, 202, runner.start(await readJsonBody(request)));
      } else if (request.method === 'GET' && parts[0] === 'runs' && parts.length === 2) {
        sendJson(response, 200, runner.find(parts[1]));
      } else if (request.method === 'POST' && parts[0] === 'runs' && parts[2] === 'cancel' && parts.length === 3) {
        sendJson(response, 202, runner.cancel(parts[1]));
      } else if (request.method === 'GET' && parts[0] === 'runs' && parts[2] === 'archive' && parts.length === 3) {
        streamArchive(response, runner.runDirectory(runner.find(parts[1]).id), parts[1]);
      } else if (request.method === 'GET' && parts[0] === 'runs' && parts[2] === 'files' && parts.length === 4) {
        const path = runner.filePath(parts[1], parts[3]);
        response.writeHead(200, { 'Content-Type': RUN_FILES[parts[3]] });
        createReadStream(path).pipe(response);
      } else {
        throw new RequestError(404, `${request.method} ${url.pathname} is not supported`);
      }
    } catch (error) {
      if (error instanceof RequestError) {
        sendJson(response, error.status, { error: error.message, ...error.details });
      } else {
        sendJson(response, 500, { error: error.message });
      }
    }
  });
}

if (import.meta.url === pathToFileURL(process.argv[1]).href) {
  const testsDirectory = join(dirname(fileURLToPath(import.meta.url)), '..');
  const runner = createRunner({
    testsDirectory,
    resultsDirectory: process.env.K6_RUNNER_RESULTS || '/results',
    k6Binary: process.env.K6_BINARY || 'k6',
  });
  const port = Number(process.env.PORT || 8080);
  createRunnerServer(runner).listen(port, () => console.log(`k6 runner listens on ${port}`));
  const stop = () => process.exit(0);
  process.on('SIGTERM', stop);
  process.on('SIGINT', stop);
}
