import importlib.util
from pathlib import Path
import unittest
import json
import subprocess
import tempfile
from unittest.mock import patch
import urllib.error
import contextlib
import io
import os
import zipfile

spec = importlib.util.spec_from_file_location("publication", Path(__file__).resolve().parents[2] / "scripts/publication.py")
publication = importlib.util.module_from_spec(spec)
spec.loader.exec_module(publication)


class PublicationTests(unittest.TestCase):
    def test_prepared_classifier_preserves_existing_namespace_and_relocation_rules(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            module = {"path": "example", "kind": "thrift"}
            target = root / "example/target"
            target.mkdir(parents=True)
            (root / "example/.flattened-pom.xml").write_text('<project xmlns="http://maven.apache.org/POM/4.0.0"><version>1.2-test</version></project>')
            for name in ["example-sources.jar", "example-javadoc.jar"]:
                (target / name).touch()
            with zipfile.ZipFile(target / "example.jar", "w") as jar:
                jar.writestr("dev/vality/damsel/Generated.class", b"class")
            for pattern, classname in [("dev.vality.damsel", "dev/vality/moved/v2/Generated.class"),
                                       ("dev.vality.another", "dev/vality/damsel/Generated.class")]:
                (root / "example/pom.xml").write_text('<project xmlns="http://maven.apache.org/POM/4.0.0"><build><relocation><pattern>' + pattern + '</pattern><shadedPattern>dev.vality.moved.v${commit.number}</shadedPattern></relocation></build></project>')
                with zipfile.ZipFile(target / "example-1.2-test-v2.jar", "w") as jar:
                    jar.writestr(classname, b"class")
                with patch.object(publication, "ROOT", root):
                    publication.verify_prepared(module, "example", "1.2-test", 2, "example")
            with zipfile.ZipFile(target / "example-1.2-test-v2.jar", "w") as jar:
                jar.writestr("wrong/package/Generated.class", b"class")
            with patch.object(publication, "ROOT", root):
                with self.assertRaisesRegex(RuntimeError, "missing the expected class"):
                    publication.verify_prepared(module, "example", "1.2-test", 2, "example")

    def run_main(self, *, available, prepare=False, check=False, profile=None, returncode=0):
        argv = ["publication.py", "packages/protocols/fraudbusters-notificator-proto"]
        if prepare:
            argv.append("--prepare")
        if check:
            argv.append("--check")
        if profile is not None:
            argv += ["--profile", profile]
        values = {"project.groupId": "dev.vality", "project.artifactId": "fraudbusters-notificator-proto",
                  "project.version": "1.33-example", "project.build.finalName": "fraudbusters-notificator-proto"}
        with patch("sys.argv", argv), patch.object(publication, "release_identity", return_value=(33, "1.33-example")), \
                patch.object(publication, "maven_property", side_effect=lambda *args: values[args[-1]]), \
                patch.object(publication, "artifact_exists", side_effect=available) as exists, \
                patch.object(publication.subprocess, "run", return_value=subprocess.CompletedProcess([], returncode)) as run, \
                patch.object(publication, "wait_for_release") as wait, \
                patch.object(publication, "verify_prepared") as verify, \
                patch.object(publication, "git", return_value=""), contextlib.redirect_stdout(io.StringIO()):
            publication.main()
            return run, wait, verify, exists

    def test_existing_complete_release_is_skipped_without_credentials(self):
        with patch.dict(os.environ, {}, clear=True):
            run, wait, _, _ = self.run_main(available=[True, True, True])
        run.assert_not_called()
        wait.assert_not_called()

    def test_partial_existing_release_is_never_overwritten(self):
        with self.assertRaisesRegex(RuntimeError, "Incomplete existing release"):
            self.run_main(available=[True, True, False])

    def test_check_missing_release_never_deploys(self):
        with patch.dict(os.environ, {}, clear=True):
            run, wait, _, _ = self.run_main(available=[False, False, False], check=True)
        run.assert_not_called()
        wait.assert_not_called()

    def test_prepare_uses_release_profile_without_signing_or_registry_writes(self):
        with patch.dict(os.environ, {}, clear=True):
            run, wait, verify, exists = self.run_main(available=[], prepare=True, profile="")
        command = run.call_args.args[0]
        self.assertIn("verify", command)
        self.assertNotIn("deploy", command)
        self.assertIn("-Pdeploy,common,central-release", command)
        self.assertIn("-Dgpg.skip=true", command)
        verify.assert_called_once()
        exists.assert_not_called()
        wait.assert_not_called()

    def test_publish_keeps_version_classifier_and_secret_out_of_command_line(self):
        secret = "unit-test-passphrase-never-in-argv"
        with patch.dict(os.environ, {"MAVEN_USERNAME": "test", "MAVEN_PASSWORD": "test-token",
                                    "MAVEN_GPG_PASSPHRASE": secret}):
            run, wait, _, _ = self.run_main(available=[False, False, False])
        command = run.call_args.args[0]
        self.assertIn("deploy", command)
        self.assertIn("-Drevision=1.33-example", command)
        self.assertIn("-Dcommit.number=33", command)
        self.assertIn("-Pdeploy,common,central-release", command)
        self.assertNotIn(secret, " ".join(command))
        urls = wait.call_args.args[0]
        self.assertTrue(any(url.endswith("-v33.jar.asc") for url in urls))
        self.assertTrue(any(url.endswith("-sources.jar.asc") for url in urls))
        self.assertTrue(any(url.endswith("-javadoc.jar.asc") for url in urls))

    def test_failed_deployment_stops_before_verification_and_does_not_expose_secret(self):
        secret = "unit-test-secret"
        with patch.dict(os.environ, {"MAVEN_USERNAME": "test", "MAVEN_PASSWORD": "test-token",
                                    "MAVEN_GPG_PASSPHRASE": secret}):
            with self.assertRaisesRegex(RuntimeError, "exit code 1") as error:
                self.run_main(available=[False, False, False], returncode=1)
        self.assertNotIn(secret, str(error.exception))

    def test_new_release_requires_portal_credentials(self):
        with patch.dict(os.environ, {}, clear=True):
            with self.assertRaisesRegex(RuntimeError, "MAVEN_USERNAME, MAVEN_PASSWORD, MAVEN_GPG_PASSPHRASE"):
                self.run_main(available=[False, False, False])

    def test_publication_waits_for_central_visibility(self):
        with patch.object(publication, "artifact_exists", side_effect=[False, True]), \
                patch.object(publication.time, "sleep") as sleep:
            publication.wait_for_release(["https://registry.invalid/artifact.jar"], attempts=2, delay=1)
        sleep.assert_called_once_with(1)

    def test_publication_verification_timeout_is_failure(self):
        with patch.object(publication, "artifact_exists", return_value=False), \
                patch.object(publication.time, "sleep"):
            with self.assertRaisesRegex(RuntimeError, "not yet readable"):
                publication.wait_for_release(["https://registry.invalid/artifact.jar"], attempts=2, delay=0)

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

    def test_unexpected_success_status_is_not_treated_as_an_absent_release(self):
        for code in [201, 202, 204, 206]:
            with patch.object(publication.urllib.request, "urlopen") as request:
                request.return_value.__enter__.return_value.status = code
                with self.assertRaisesRegex(RuntimeError, "Unexpected Central response"):
                    publication.artifact_exists("https://registry.invalid")

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

    def test_shallow_checkout_requires_full_history_and_recovers_the_same_release(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory) / "source"
            root.mkdir()
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
            (root / path / "contract.thrift").write_text("changed")
            commit("contract change")
            (root / "README.md").write_text("unrelated")
            commit("unrelated change")
            module = {"path": path}
            with patch.object(publication, "ROOT", root):
                expected = publication.release_identity(module)
            shallow = Path(directory) / "checkout"
            subprocess.run(["git", "clone", "-q", "--depth", "1", root.as_uri(), str(shallow)], check=True)
            with patch.object(publication, "ROOT", shallow):
                with self.assertRaisesRegex(RuntimeError, "fetch-depth: 0"):
                    publication.release_identity(module)
                subprocess.run(["git", "fetch", "-q", "--unshallow"], cwd=shallow, check=True)
                self.assertEqual(publication.release_identity(module), expected)
