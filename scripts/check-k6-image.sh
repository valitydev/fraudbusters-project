#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
image=${1:?Usage: check-k6-image.sh <image>}
for script in tests/k6/suites/*/main.js tests/k6/tools/ready.js; do
    docker run --rm --entrypoint k6 "$image" inspect -e RUN_ID=check "/k6/${script#tests/k6/}" >/dev/null \
        || { echo "$script does not load in $image" >&2; exit 1; }
done
echo "Every suite loads in $image"
docker run --rm --entrypoint node "$image" --test /k6/runner/runner.test.mjs >/dev/null \
    || { echo "Runner tests fail in $image" >&2; exit 1; }
echo "Runner tests pass in $image"
container=$(docker run -d --rm "$image")
trap 'docker rm -f "$container" >/dev/null 2>&1 || true' EXIT
for attempt in $(seq 1 20); do
    if docker exec "$container" wget -qO- http://127.0.0.1:8080/health >/dev/null 2>&1; then
        echo "Runner answers in $image after $attempt s"
        exit 0
    fi
    sleep 1
done
echo "Runner does not answer in $image" >&2
docker logs "$container" >&2
exit 1
