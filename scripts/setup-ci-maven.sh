#!/usr/bin/env bash
# Keep publishing compatible with the upstream Central plugin and Maven 3 model.
set -euo pipefail
: "${RUNNER_TEMP:?This installer is intended for GitHub Actions}"
: "${GITHUB_PATH:?GitHub Actions path file is required}"
version=3.9.12
directory="$RUNNER_TEMP/apache-maven-$version"
if [[ ! -x "$directory/bin/mvn" ]]; then
    archive="$RUNNER_TEMP/apache-maven-$version-bin.tar.gz"
    url="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$version/apache-maven-$version-bin.tar.gz"
    curl --fail --silent --show-error --location --retry 3 "$url" -o "$archive"
    checksum=$(curl --fail --silent --show-error --location --retry 3 "$url.sha512")
    printf '%s  %s\n' "$checksum" "$archive" | shasum -a 512 --check --status
    tar -xzf "$archive" -C "$RUNNER_TEMP"
    rm -f "$archive"
fi
printf '%s\n' "$directory/bin" >> "$GITHUB_PATH"
"$directory/bin/mvn" --version
