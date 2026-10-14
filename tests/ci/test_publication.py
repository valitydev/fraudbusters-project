import importlib.util
from pathlib import Path
import unittest
import json
import subprocess
import tempfile
from unittest.mock import patch
import urllib.error

spec = importlib.util.spec_from_file_location("publication", Path(__file__).resolve().parents[2] / "scripts/publication.py")
publication = importlib.util.module_from_spec(spec)
spec.loader.exec_module(publication)


class PublicationTests(unittest.TestCase):
    def test_only_404_is_treated_as_absent(self):
        for code in [401, 403, 429, 500, 503]:
            error = urllib.error.HTTPError("https://registry.invalid", code, "error", {}, None)
            with patch.object(publication.urllib.request, "urlopen", side_effect=error):
                with self.assertRaises(urllib.error.HTTPError):
                    publication.artifact_exists("https://registry.invalid")
        with patch.object(publication.urllib.request, "urlopen", side_effect=urllib.error.HTTPError("https://registry.invalid", 404, "absent", {}, None)):
            self.assertFalse(publication.artifact_exists("https://registry.invalid"))

    def test_existing_release_is_detected(self):
        with patch.object(publication.urllib.request, "urlopen") as request:
            request.return_value.__enter__.return_value.status = 200
            self.assertTrue(publication.artifact_exists("https://registry.invalid"))

    def test_network_error_does_not_trigger_publication(self):
        with patch.object(publication.urllib.request, "urlopen", side_effect=urllib.error.URLError("offline")):
            with self.assertRaises(urllib.error.URLError):
                publication.artifact_exists("https://registry.invalid")

    def test_unrelated_commits_do_not_allocate_a_new_release(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            def git(*args):
                return subprocess.check_output(["git", *args], cwd=root, text=True).strip()
            def commit(message):
                git("add", ".")
                git("commit", "-qm", message)
                return git("rev-parse", "HEAD")
            git("init", "-q")
            git("config", "user.name", "Test")
            git("config", "user.email", "test@example.invalid")
            (root / "README.md").write_text("source")
            source = commit("source")
            path = "packages/protocols/example"
            (root / path).mkdir(parents=True)
            (root / path / "contract.thrift").write_text("initial")
            commit("Import example master at " + source)
            (root / "docs/migration").mkdir(parents=True)
            (root / "docs/migration/sources.json").write_text(json.dumps({"sources": [{"name": "example", "sha": source, "path": path}]}))
            commit("manifest")
            module = {"path": path}
            with patch.object(publication, "ROOT", root):
                initial = publication.release_identity(module)
                (root / "README.md").write_text("unrelated service changes")
                commit("unrelated")
                self.assertEqual(publication.release_identity(module), initial)
                (root / path / "contract.thrift").write_text("new contract")
                changed = commit("contract change")
                self.assertEqual(publication.release_identity(module), (initial[0] + 1, f"1.{initial[0] + 1}-{changed[:7]}"))
                (root / "README.md").write_text("another unrelated change")
                commit("unrelated again")
                self.assertEqual(publication.release_identity(module)[1], f"1.{initial[0] + 1}-{changed[:7]}")
