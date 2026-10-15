import importlib.util
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location("gate", ROOT / "scripts/check-publication.py")
gate = importlib.util.module_from_spec(spec)
spec.loader.exec_module(gate)


class PublicationGateTests(unittest.TestCase):
    def needs(self, apps=True, ui=True, packages=True):
        return {
            "affected": {"result": "success", "outputs": {
                "java_apps": json.dumps(["fraudbusters"] if apps else []),
                "ui_changed": "true" if ui else "false",
                "publish": json.dumps([{"path": "packages/example"}] if packages else []),
            }},
            "checks": {"result": "success"},
            "java-images": {"result": "success" if apps else "skipped"},
            "ui-image": {"result": "success" if ui else "skipped"},
            "publish-packages": {"result": "success" if packages else "skipped"},
        }

    def test_success_and_legitimate_skips(self):
        for selection in [(True, True, True), (True, True, False), (False, False, True), (False, False, False)]:
            for event in ["push", "workflow_dispatch"]:
                self.assertEqual(gate.failures(self.needs(*selection), event, "master", "master"), [])

    def test_selected_publishers_cannot_be_silently_skipped_or_fail(self):
        for job in ["java-images", "ui-image", "publish-packages"]:
            for result in ["skipped", "failure", "cancelled"]:
                needs = self.needs()
                needs[job]["result"] = result
                self.assertTrue(gate.failures(needs, "push", "master", "master"))

    def test_pr_and_non_default_branch_do_not_require_publications(self):
        needs = self.needs()
        for job in ["java-images", "ui-image", "publish-packages"]:
            needs[job]["result"] = "skipped"
        self.assertEqual(gate.failures(needs, "pull_request", "123/merge", "master"), [])
        self.assertEqual(gate.failures(needs, "workflow_dispatch", "feature", "master"), [])

    def test_failed_validation_cannot_look_like_successful_release(self):
        for job in ["affected", "checks"]:
            needs = self.needs()
            needs[job]["result"] = "failure"
            self.assertTrue(gate.failures(needs, "push", "master", "master"))

    def test_workflow_publishers_override_skip_propagation_and_guard_validation(self):
        workflow = (ROOT / ".github/workflows/ci.yml").read_text()
        for job in ["java-images", "ui-image", "publish-packages"]:
            block = workflow.split("\n  " + job + ":\n", 1)[1].split("    permissions:", 1)[0]
            # publish-packages has no permissions block; only inspect its if expression.
            condition = block.split("    if: >-\n", 1)[1].split("    runs-on:", 1)[0]
            self.assertIn("!cancelled()", condition)
            self.assertIn("needs.checks.result == 'success'", condition)
            self.assertIn("needs.affected.result == 'success'", condition)
            self.assertIn("github.event_name != 'pull_request'", condition)
            self.assertIn("github.ref_name == github.event.repository.default_branch", condition)
