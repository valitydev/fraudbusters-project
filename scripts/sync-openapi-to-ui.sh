#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
contract=packages/protocols/swag-fraudbusters-management
if [[ ! -d "$contract/node_modules" ]]; then npm ci --prefix "$contract"; fi
npm run build --prefix "$contract"
cp "$contract/dist/openapi.json" apps/fraudbusters-ui/fraudbusters-management.json
