#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
# Use a dedicated Compose project, leaving other developer environments intact.
compose_files=(-f infra/compose.yaml)
if [[ "$(uname -m)" == arm64 || "$(uname -m)" == aarch64 ]]; then
    compose_files+=(-f infra/compose.arm.yaml)
fi
exec docker compose -p fraudbusters-project-e2e "${compose_files[@]}" \
    --profile apps --profile e2e up --build \
    --abort-on-container-exit --exit-code-from e2e-test e2e-test
