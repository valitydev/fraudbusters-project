#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
app=${1:?Usage: package-java-image.sh APP OUTPUT}
output=${2:?Usage: package-java-image.sh APP OUTPUT}
[[ "$app" =~ ^[a-z][a-z0-9-]+$ && -f "apps/$app/pom.xml" ]] || exit 2
[[ ! -e "$output" ]] || { echo 'Output must not exist' >&2; exit 1; }
python3 scripts/package_java_image.py "$app" "$output"
