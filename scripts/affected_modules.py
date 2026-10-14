#!/usr/bin/env python3
"""Select independent published-dependency builds from Git paths."""
import argparse
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def catalog():
    return json.loads((ROOT / "scripts/modules.json").read_text())


def select(paths, mode="changed"):
    modules = catalog()
    selected = set()
    images = set()
    publish = set()
    openapi = set()
    ui = False
    infra = False
    docs = False
    for path in paths:
        if path in {"pom.xml", "package.json", ".dockerignore"} or path.startswith(("scripts/", ".github/workflows/", "tests/ci/", "infra/docker/")):
            selected.update(m["path"] for m in modules if "java" in m)
            images.update(m["name"] for m in modules if m["kind"] == "service")
            openapi.update(m["path"] for m in modules if m["kind"] == "openapi")
            ui = infra = True
        if path.startswith(("infra/", "tests/e2e/")):
            infra = True
        if path.startswith("docs/"):
            docs = True
        for module in modules:
            if not path.startswith(module["path"] + "/"):
                continue
            kind = module["kind"]
            if "java" in module:
                selected.add(module["path"])
            if kind == "service":
                images.add(module["name"])
            elif kind == "ui":
                ui = True
            elif kind in {"thrift", "openapi", "library"}:
                publish.add(module["path"])
                if kind == "openapi":
                    openapi.add(module["path"])
                    if module["name"] == "swag-fraudbusters-management":
                        ui = True
    if mode in {"all", "images"}:
        images.update(m["name"] for m in modules if m["kind"] == "service")
        selected.update(m["path"] for m in modules if m["kind"] == "service")
        ui = True
    if mode in {"all", "contracts"}:
        publish.update(m["path"] for m in modules if "publish" in m)
        selected.update(m["path"] for m in modules if "publish" in m)
        openapi.update(m["path"] for m in modules if m["kind"] == "openapi")
    if mode == "all":
        selected.update(m["path"] for m in modules if "java" in m)
        infra = docs = True
    java = []
    for module in modules:
        if module["path"] not in selected:
            continue
        for profile in module.get("profiles", [""]):
            java.append({"path": module["path"], "java": module["java"], "kind": module["kind"], "profile": profile})
    return {
        "java": java,
        "java_apps": sorted(images),
        "publish": [m for m in modules if m["path"] in publish],
        "openapi": sorted(openapi),
        "erlang": [m["path"] for m in modules if m["path"] in selected and m.get("erlang")],
        "ui_changed": ui,
        "infra_changed": infra,
        "docs_changed": docs,
    }


def changed_paths(base, head="HEAD", pull_request=False):
    # An initial push has no before SHA: include every tracked file.
    if not base or set(base) == {"0"}:
        args = ["git", "ls-tree", "-r", "--name-only", "-z", head]
    else:
        comparison = base + ("..." if pull_request else "..") + head
        # No rename detection: both deleted and added paths select their owners.
        args = ["git", "diff", "--no-renames", "--name-only", "-z", comparison]
    output = subprocess.check_output(args, cwd=ROOT)
    return [p.decode() for p in output.split(b"\0") if p]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("base", nargs="?")
    parser.add_argument("--head", default="HEAD")
    parser.add_argument("--pr", action="store_true")
    parser.add_argument("--mode", choices=["changed", "all", "images", "contracts"], default="changed")
    parser.add_argument("--github-output", action="store_true")
    args = parser.parse_args()
    if args.mode == "changed" and not args.base:
        parser.error("base SHA is required for changed mode")
    paths = changed_paths(args.base, args.head, args.pr) if args.mode == "changed" else []
    result = select(paths, args.mode)
    if args.github_output:
        for key, value in result.items():
            print(f"{key}={json.dumps(value, separators=(',', ':'))}")
    else:
        print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
