#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
apps=(fraudbusters fraudbusters-api fraudbusters-management fraudbusters-mg-connector fraudbusters-notificator fraudbusters-warehouse trusted-tokens-manager wb-list-manager)
if (( $# )); then apps=("$@"); fi
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
for app in "${apps[@]}"; do
    mvn -B -ntp -f "apps/$app/pom.xml" -P=-common package -DskipTests
    scripts/package-java-image.sh "$app" "$work/$app"
    docker build -t "ghcr.io/valitydev/$app:local" "$work/$app"
done
if (( $# == 0 )); then
    npm run ui:install
    npm run ui:build
    docker build -t ghcr.io/valitydev/fraudbusters-ui:local apps/fraudbusters-ui
fi
