#!/usr/bin/env python3
"""Immutable module releases; no network writes occur in --check mode."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import time
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def git(*args):
    return subprocess.check_output(["git", *args], cwd=ROOT, text=True).strip()


def release_identity(module):
    if git("rev-parse", "--is-shallow-repository") == "true":
        raise RuntimeError("Release versioning requires full Git history; configure actions/checkout with fetch-depth: 0")
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
        with urllib.request.urlopen(urllib.request.Request(url, method="HEAD"), timeout=30) as response:
            if response.status != 200:
                raise RuntimeError("Unexpected Central response: " + str(response.status))
            return True
    except urllib.error.HTTPError as error:
        if error.code == 404:
            return False
        raise


def release_profiles(module, profile):
    profiles = ["deploy", "common"]
    pom = ET.parse(ROOT / module["path"] / "pom.xml")
    ns = {"m": "http://maven.apache.org/POM/4.0.0"}
    if any(node.text == "central-release" for node in pom.findall("m:profiles/m:profile/m:id", ns)):
        profiles.append("central-release")
    if profile:
        profiles.append(profile)
    return ",".join(profiles)


def build_command(module, profile, revision, count, prepare=False):
    command = ["mvn", "-B", "-ntp", "-f", module["path"] + "/pom.xml", "clean",
               "verify" if prepare else "deploy", "-P" + release_profiles(module, profile),
               "-Drevision=" + revision, "-Dcommit.number=" + str(count), "-Dpath_to_thrift=thrift"]
    # GPG 3.2+ reads MAVEN_GPG_PASSPHRASE directly; never put it in argv/logs.
    command += ["-Dgpg.bestPractices=true", "-Dgpg.passphraseEnvName=MAVEN_GPG_PASSPHRASE"]
    if prepare:
        command.append("-Dgpg.skip=true")
    return command


def release_files(artifact, version, kind, count, attachments=False):
    stem = artifact + "-" + version
    files = [stem + ".pom", stem + ".jar"]
    if kind == "thrift":
        files.append(stem + "-v" + str(count) + ".jar")
    if attachments:
        files += [stem + "-sources.jar", stem + "-javadoc.jar"]
    return files


def verify_prepared(module, artifact, version, count, final_name):
    target = ROOT / module["path"] / "target"
    # Local build.finalName need not include a version. Central uses GAV filenames.
    files = [final_name + suffix for suffix in [".jar", "-sources.jar", "-javadoc.jar"]]
    if module["kind"] == "thrift":
        files.append(artifact + "-" + version + "-v" + str(count) + ".jar")
    missing = [name for name in files if not (target / name).is_file()]
    if missing:
        raise RuntimeError("Missing release attachments: " + ", ".join(missing))
    flattened = ROOT / module["path"] / ".flattened-pom.xml"
    ns = {"m": "http://maven.apache.org/POM/4.0.0"}
    if not flattened.exists() or ET.parse(flattened).findtext("m:version", namespaces=ns) != version:
        raise RuntimeError("Release POM must contain the resolved version: " + version)
    if module["kind"] == "thrift":
        pom = ET.parse(ROOT / module["path"] / "pom.xml")
        relocations = []
        for node in pom.findall(".//m:relocation", ns):
            before = node.findtext("m:pattern", namespaces=ns).replace(".", "/") + "/"
            after = node.findtext("m:shadedPattern", namespaces=ns).replace("${commit.number}", str(count)).replace(".", "/") + "/"
            relocations.append((before, after))
        with zipfile.ZipFile(target / files[0]) as main_jar, zipfile.ZipFile(target / files[-1]) as shaded:
            actual = set(shaded.namelist())
            for name in main_jar.namelist():
                if not name.endswith(".class"):
                    continue
                expected = name
                for before, after in relocations:
                    if name.startswith(before):
                        expected = after + name[len(before):]
                        break
                # Existing mappings can legitimately be no-ops for other namespaces.
                if expected not in actual:
                    raise RuntimeError("Thrift classifier is missing the expected class: " + expected)


def wait_for_release(urls, attempts=31, delay=10):
    for attempt in range(attempts):
        missing = [url for url in urls if not artifact_exists(url)]
        if not missing:
            return
        if attempt + 1 < attempts:
            time.sleep(delay)
    raise RuntimeError("Published artifacts are not yet readable from Central: " + ", ".join(missing))


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
    parser.add_argument("--prepare", action="store_true", help="Verify release packaging without signing or uploading")
    parser.add_argument("--profile", help="Select one declared profile, including an empty profile for Thrift/Fraudo")
    args = parser.parse_args()
    if args.check and args.prepare:
        parser.error("--check and --prepare cannot be combined")
    modules = json.loads((ROOT / "scripts/modules.json").read_text())
    module = next(m for m in modules if m["path"] == args.module and "publish" in m)
    count, revision = release_identity(module)
    profiles = module.get("profiles", [""])
    if args.profile is not None:
        if args.profile not in profiles:
            parser.error("Profile is not declared for this module")
        profiles = [args.profile]
    for profile in profiles:
        group = maven_property(module, profile, revision, "project.groupId")
        artifact = maven_property(module, profile, revision, "project.artifactId")
        version = maven_property(module, profile, revision, "project.version")
        if "SNAPSHOT" in version:
            raise RuntimeError("Refusing to publish mutable SNAPSHOT version")
        if args.prepare:
            result = subprocess.run(build_command(module, profile, revision, count, prepare=True), cwd=ROOT)
            if result.returncode:
                raise RuntimeError(f"Release preparation failed with exit code {result.returncode}")
            final_name = maven_property(module, profile, revision, "project.build.finalName")
            verify_prepared(module, artifact, version, count, final_name)
            print(f"{artifact}:{version}: release packaging verified (no signing/upload)", flush=True)
            continue
        base = "https://repo.maven.apache.org/maven2/" + group.replace(".", "/") + f"/{artifact}/{version}/"
        urls = [base + name for name in release_files(artifact, version, module["kind"], count)]
        available = [artifact_exists(url) for url in urls]
        exists = all(available)
        if any(available) and not exists:
            raise RuntimeError("Incomplete existing release; refusing to overwrite " + artifact + ":" + version)
        print(f"{artifact}:{version}: " + ("already published" if exists else "missing"), flush=True)
        if exists or args.check:
            continue
        missing = [name for name in ["MAVEN_USERNAME", "MAVEN_PASSWORD", "MAVEN_GPG_PASSPHRASE"] if not os.environ.get(name)]
        if missing:
            raise RuntimeError("Central Portal publishing requires: " + ", ".join(missing))
        if git("status", "--porcelain", "--", module["path"]):
            raise RuntimeError("Commit module changes before publishing an immutable release")
        result = subprocess.run(build_command(module, profile, revision, count), cwd=ROOT)
        if result.returncode:
            raise RuntimeError(f"Maven deployment failed with exit code {result.returncode}")
        files = release_files(artifact, version, module["kind"], count, attachments=True)
        wait_for_release([base + name for name in files + [name + ".asc" for name in files]])
        print(f"{artifact}:{version}: published artifacts and signatures are readable", flush=True)


if __name__ == "__main__":
    main()
