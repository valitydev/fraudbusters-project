#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
image=${1:?Usage: check-k6-image.sh <image>}
for script in tests/k6/suites/*/main.js tests/k6/tools/ready.js; do
    docker run --rm "$image" inspect -e RUN_ID=check "/k6/${script#tests/k6/}" >/dev/null \
        || { echo "$script does not load in $image" >&2; exit 1; }
done
echo "Every suite loads in $image"
