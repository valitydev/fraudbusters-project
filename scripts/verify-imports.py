#!/usr/bin/env python3
"""Verify immutable subtree import commits against the recorded source trees."""
import json
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
manifest = json.loads((root / "docs/migration/sources.json").read_text())
for source in manifest["sources"]:
    def git(*args):
        return subprocess.check_output(["git", *args], cwd=root, text=True).strip()
    sha, path = source["sha"], source["path"]
    commits = git("log", "--all", "--format=%H", "--fixed-strings", "--grep=Import " + source["name"] + " master at " + sha).splitlines()
    if len(commits) != 1:
        raise SystemExit(f"Expected one import commit for {path}, got {len(commits)}")
    assert git("rev-parse", sha + "^{tree}") == source["tree"], path
    assert git("rev-parse", commits[0] + ":" + path) == source["tree"], path
    subprocess.run(["git", "merge-base", "--is-ancestor", sha, "HEAD"], cwd=root, check=True)
    print(f"Verified {path}: {sha}")
