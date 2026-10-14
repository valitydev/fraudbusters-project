#!/usr/bin/env python3
"""Compare current source files (including local edits) with the import manifest.

This is a content audit, not a guarantee of runtime compatibility. Known file moves
are resolved explicitly; generated/ignored files are not treated as source additions.
"""
import argparse
from collections import Counter
import hashlib
import json
import os
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]


def git(*args):
    return subprocess.check_output(["git", *args], cwd=ROOT)


def destination(source, path):
    prefix = source["path"]
    if source["name"] == "fraudo" and (
        path.startswith("content/")
        or path in {"logo.jpg", "syntax.png", "mkdocs.yml", "requirements.txt"}
    ):
        return "docs/fraudo/" + path
    if source["name"] == "fraudbusters-compose":
        if path.startswith("e2e-test/"):
            return "tests/e2e/" + path[len("e2e-test/"):]
        path = {"docker-compose.yml": "compose.yaml",
                "docker-compose-arm.yml": "compose.arm.yaml"}.get(path, path)
    return prefix + "/" + path


def blob_hash(data):
    return hashlib.sha1(b"blob " + str(len(data)).encode() + b"\0" + data).hexdigest()


def xml_signature(element):
    if element is None:
        return None
    return (element.tag, (element.text or "").strip(),
            tuple(xml_signature(child) for child in element))


def maven_metadata(source):
    path = ROOT / source["path"] / "pom.xml"
    if not path.exists():
        return None
    before = ET.fromstring(git("show", source["sha"] + ":pom.xml"))
    after = ET.parse(path).getroot()
    ns = {"m": "http://maven.apache.org/POM/4.0.0"}
    fields = ["parent", "groupId", "artifactId", "version", "dependencies", "dependencyManagement"]
    equal = all(xml_signature(before.find("m:" + name, ns)) ==
                xml_signature(after.find("m:" + name, ns)) for name in fields)
    old_properties = before.find("m:properties", ns)
    new_properties = after.find("m:properties", ns)
    properties_equal = old_properties is None or all(
        new_properties is not None and xml_signature(child) ==
        xml_signature(new_properties.find(child.tag)) for child in old_properties)
    return {"name": source["name"], "coordinates_parent_dependencies_equal": equal,
            "existing_properties_equal": properties_equal}


def audit():
    manifest = json.loads((ROOT / "docs/migration/sources.json").read_text())
    mapped = set()
    modules = []
    pom_metadata = []
    protected = Counter()
    protected_differences = []
    for source in manifest["sources"]:
        counts = Counter()
        differences = []
        for entry in git("ls-tree", "-r", "-z", source["sha"]).split(b"\0"):
            if not entry:
                continue
            metadata, raw_path = entry.split(b"\t", 1)
            mode, kind, sha = metadata.decode().split()
            original = raw_path.decode()
            relative = destination(source, original)
            mapped.add(relative)
            path = ROOT / relative
            moved = relative != source["path"] + "/" + original
            if kind == "commit":
                current = git("ls-files", "--stage", "--", relative).decode().split()
                status = "gitlink_pinned" if current and current[1] == sha else "gitlink_changed"
            elif not path.exists() and not path.is_symlink():
                status = "missing"
            else:
                data = os.readlink(path).encode() if path.is_symlink() else path.read_bytes()
                actual_mode = "120000" if path.is_symlink() else (
                    "100755" if path.stat().st_mode & 0o111 else "100644")
                equal = blob_hash(data) == sha and actual_mode == mode
                status = ("moved_identical" if moved else "identical") if equal else (
                    "moved_modified" if moved else "modified")
            counts[status] += 1
            category = None
            if original.endswith(".thrift") or original.startswith("openapi/"):
                category = "contract_sources"
            elif "/db/migration/" in original:
                category = "database_migrations"
            elif original.startswith("src/main/resources/") and source["path"].startswith("apps/"):
                category = "backend_runtime_resources"
            elif source["name"] == "fraudo" and original.startswith("src/main/"):
                category = "fraudo_runtime_sources"
            if category:
                protected[category] += 1
                if status not in {"identical", "moved_identical"}:
                    protected_differences.append(relative)
            if status not in {"identical", "gitlink_pinned"}:
                differences.append({"source": original, "destination": relative, "status": status})
        modules.append({"name": source["name"], "sha": source["sha"],
                        "counts": dict(counts), "differences": differences})
        pom = maven_metadata(source)
        if pom:
            pom_metadata.append(pom)
    current = git("ls-files", "--cached", "--others", "--exclude-standard", "-z",
                  "apps", "packages", "infra", "tests/e2e", "docs").decode().split("\0")
    additions = sorted({p for p in current if p and p not in mapped and (ROOT / p).exists()})
    return {"head": git("rev-parse", "HEAD").decode().strip(),
            "includes_worktree_changes": True, "modules": modules,
            "added_files": additions, "maven_metadata": pom_metadata,
            "protected_source_counts": dict(protected),
            "protected_source_differences": protected_differences}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    result = audit()
    if args.output:
        args.output.write_text(json.dumps(result, indent=2, ensure_ascii=False) + "\n")
    for module in result["modules"]:
        print(module["name"] + ": " + json.dumps(module["counts"], sort_keys=True))
    print("Added source files: " + str(len(result["added_files"])))


if __name__ == "__main__":
    main()
