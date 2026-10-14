#!/usr/bin/env python3
"""Immutable module releases; no network writes occur in --check mode."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parents[1]


def git(*args):
    return subprocess.check_output(["git", *args], cwd=ROOT, text=True).strip()


def release_identity(module):
    sources = json.loads((ROOT / "docs/migration/sources.json").read_text())["sources"]
    source = next(s for s in sources if s["path"] == module["path"])
    imports = git("log", "--first-parent", "--format=%H", "--fixed-strings", "--grep=Import " + source["name"] + " master at " + source["sha"]).splitlines()
    if len(imports) != 1:
        raise RuntimeError("Missing unique import commit")
    changes = git("rev-list", "--first-parent", imports[0] + "..HEAD", "--", module["path"]).splitlines()
    count = int(git("rev-list", "--count", source["sha"])) + len(changes)
    sha = changes[0] if changes else source["sha"]
    return count, f"1.{count}-{sha[:7]}"


def artifact_exists(url):
    # Only 404 means absent. Authentication/server/network failures must fail closed.
    try:
        with urllib.request.urlopen(url, timeout=30) as response:
            return response.status == 200
    except urllib.error.HTTPError as error:
        if error.code == 404:
            return False
        raise


def maven_property(module, profile, revision, expression):
    command = ["mvn", "-B", "-q", "-ntp", "-f", module["path"] + "/pom.xml", "help:evaluate", "-DforceStdout", "-Dexpression=" + expression, "-Drevision=" + revision]
    if profile:
        command.append("-P" + profile)
    result = subprocess.check_output(command, cwd=ROOT, text=True)
    import re
    result = re.sub(r"\x1b\[[0-9;]*m", "", result).strip()
    # Maven warnings are emitted before the evaluated property.
    result = result.splitlines()[-1]
    if "null object" in result or "${" in result:
        raise RuntimeError("Unresolved Maven property: " + expression)
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("module")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    modules = json.loads((ROOT / "scripts/modules.json").read_text())
    module = next(m for m in modules if m["path"] == args.module and "publish" in m)
    count, revision = release_identity(module)
    for profile in module.get("profiles", [""]):
        group = maven_property(module, profile, revision, "project.groupId")
        artifact = maven_property(module, profile, revision, "project.artifactId")
        version = maven_property(module, profile, revision, "project.version")
        if "SNAPSHOT" in version:
            raise RuntimeError("Refusing to publish mutable SNAPSHOT version")
        url = "https://repo.maven.apache.org/maven2/" + group.replace(".", "/") + f"/{artifact}/{version}/{artifact}-{version}.pom"
        exists = artifact_exists(url)
        print(f"{artifact}:{version}: " + ("already published" if exists else "missing"), flush=True)
        if exists or args.check:
            continue
        if not os.environ.get("MAVEN_GPG_PASSPHRASE"):
            raise RuntimeError("MAVEN_GPG_PASSPHRASE is required")
        command = ["mvn", "-B", "-ntp", "-f", module["path"] + "/pom.xml", "clean", "deploy", "-Pdeploy,common" + ("," + profile if profile else ""), "-Drevision=" + revision, "-Dcommit.number=" + str(count), "-Dpath_to_thrift=thrift", "-Dgpg.passphrase=" + os.environ["MAVEN_GPG_PASSPHRASE"], "-DautoReleaseAfterClose=true"]
        # CalledProcessError renders the full command, including the signing secret.
        result = subprocess.run(command, cwd=ROOT)
        if result.returncode:
            raise RuntimeError(f"Maven deployment failed with exit code {result.returncode}")


if __name__ == "__main__":
    main()
