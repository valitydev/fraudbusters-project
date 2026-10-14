#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
# Use a dedicated Compose project, leaving other developer environments intact.
exec docker compose -p fraudbusters-project-e2e -f infra/compose.yaml \
    --profile apps --profile e2e up --build \
    --abort-on-container-exit --exit-code-from e2e-test e2e-test
