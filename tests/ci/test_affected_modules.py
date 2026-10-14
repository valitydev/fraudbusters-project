import importlib.util
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location("affected", ROOT / "scripts/affected_modules.py")
affected = importlib.util.module_from_spec(spec)
spec.loader.exec_module(affected)


class SelectionTests(unittest.TestCase):
    def test_one_service_does_not_build_other_services(self):
        result = affected.select(["apps/fraudbusters-api/src/main/java/Example.java"])
        self.assertEqual(result["java_apps"], ["fraudbusters-api"])
        self.assertEqual([m["path"] for m in result["java"]], ["apps/fraudbusters-api"])
        self.assertEqual(result["publish"], [])

    def test_thrift_and_library_use_explicit_consumers(self):
        for path in ["packages/protocols/fraudbusters-proto/proto/inspector.thrift", "packages/libs/fraudo/pom.xml"]:
            result = affected.select([path])
            self.assertEqual(len(result["publish"]), 1)
            self.assertEqual(result["java_apps"], [])
            self.assertFalse(result["ui_changed"])

    def test_management_contract_builds_both_java_profiles_and_ui(self):
        result = affected.select(["packages/protocols/swag-fraudbusters-management/openapi/openapi.yaml"])
        self.assertEqual({m["profile"] for m in result["java"]}, {"server", "client"})
        self.assertTrue(result["ui_changed"])
        self.assertEqual(result["java_apps"], [])

    def test_inspector_contract_does_not_rebuild_ui(self):
        self.assertFalse(affected.select(["packages/protocols/swag-fraudbusters/openapi/openapi.yaml"])["ui_changed"])

    def test_shared_build_checks_all_without_publishing_unchanged_packages(self):
        result = affected.select(["scripts/modules.json"])
        self.assertEqual(len(result["java_apps"]), 8)
        self.assertEqual(len(result["java"]), 19)
        self.assertTrue(result["ui_changed"])
        self.assertEqual(result["publish"], [])

    def test_docs_and_infra_are_independent(self):
        result = affected.select(["docs/content/index.md", "infra/compose.yaml"])
        self.assertTrue(result["docs_changed"])
        self.assertTrue(result["infra_changed"])
        self.assertEqual(result["java"], [])

    def test_manual_modes(self):
        contracts = affected.select([], "contracts")
        self.assertEqual(len(contracts["publish"]), 9)
        self.assertEqual(contracts["java_apps"], [])
        images = affected.select([], "images")
        self.assertEqual(len(images["java_apps"]), 8)
        self.assertEqual(len(images["java"]), 8)
        self.assertTrue(images["ui_changed"])
        self.assertEqual(images["publish"], [])


class GitDiffTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.root_patch = patch.object(affected, "ROOT", self.root)
        self.root_patch.start()
        self.git("init", "-q")
        self.git("config", "user.name", "CI Test")
        self.git("config", "user.email", "test@example.invalid")
        self.commit("apps/fraudbusters/a", "initial")
        self.base = self.git("rev-parse", "HEAD")

    def tearDown(self):
        self.root_patch.stop()
        self.temp.cleanup()

    def git(self, *args):
        return subprocess.check_output(["git", *args], cwd=self.root, text=True).strip()

    def commit(self, path, content):
        p = self.root / path
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(content)
        self.git("add", "--all")
        self.git("commit", "-qm", content)

    def test_multi_commit_push_includes_all_changes(self):
        self.commit("apps/fraudbusters/a", "first change")
        self.commit("apps/fraudbusters-api/b", "second change")
        self.assertEqual(set(affected.changed_paths(self.base)), {"apps/fraudbusters/a", "apps/fraudbusters-api/b"})

    def test_rename_and_delete_include_both_owners(self):
        p = self.root / "apps/fraudbusters-api/a"
        p.parent.mkdir(parents=True)
        (self.root / "apps/fraudbusters/a").rename(p)
        self.git("add", "--all")
        self.git("commit", "-qm", "rename")
        self.assertEqual(set(affected.changed_paths(self.base)), {"apps/fraudbusters/a", "apps/fraudbusters-api/a"})
        p.unlink()
        self.git("add", "--all")
        self.git("commit", "-qm", "delete")
        self.assertEqual(affected.changed_paths("HEAD^"), ["apps/fraudbusters-api/a"])

    def test_pr_excludes_changes_only_on_base_branch(self):
        self.git("checkout", "-qb", "feature")
        self.commit("apps/fraudbusters-api/b", "feature")
        head = self.git("rev-parse", "HEAD")
        self.git("checkout", "-q", "--detach", self.base)
        self.commit("docs/base.md", "base only")
        base = self.git("rev-parse", "HEAD")
        self.assertEqual(affected.changed_paths(base, head, True), ["apps/fraudbusters-api/b"])

    def test_first_push_includes_all_files(self):
        self.assertEqual(affected.changed_paths("0" * 40), ["apps/fraudbusters/a"])

    def test_invalid_base_is_an_error(self):
        with self.assertRaises(subprocess.CalledProcessError):
            affected.changed_paths("missing-ref")
