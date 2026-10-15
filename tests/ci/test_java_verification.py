"""Exercise the actual workflow shell with a recording Maven executable."""
import json
import os
import re
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]


class JavaVerificationTests(unittest.TestCase):
    def test_release_preparation_checks_out_full_history(self):
        workflow = (ROOT / ".github/workflows/ci.yml").read_text()
        for job in ["java", "publish-packages"]:
            with self.subTest(job=job):
                section = re.search(r"(?ms)^  " + job + r":\n(.*?)(?=^  [\w-]+:\n|\Z)", workflow).group(1)
                checkout = section.split("      - uses: actions/checkout@v4\n", 1)[1].split("      - ", 1)[0]
                self.assertIn("fetch-depth: 0", checkout)

    def test_matrix_passes_valid_local_contract_versions_without_changing_service_versions(self):
        workflow = (ROOT / ".github/workflows/ci.yml").read_text()
        step = workflow.split("      - name: Verify independent module against published dependencies\n", 1)[1]
        step = step.split("      - name: Verify release packaging without signing or uploading\n", 1)[0]
        script = "\n".join(line[10:] for line in step.split("        run: |\n", 1)[1].splitlines())
        modules = json.loads((ROOT / "scripts/modules.json").read_text())
        with tempfile.TemporaryDirectory() as directory:
            temp = Path(directory)
            maven = temp / "mvn"
            maven.write_text('#!/usr/bin/env bash\nprintf "%s\\n" "$@" > "$CAPTURE"\n')
            maven.chmod(0o755)
            capture = temp / "arguments"
            for module in modules:
                if "java" not in module:
                    continue
                for profile in module.get("profiles", [""]):
                    with self.subTest(module=module["path"], profile=profile):
                        env = dict(os.environ, MODULE=module["path"], KIND=module["kind"],
                                   PROFILE=profile, CAPTURE=str(capture),
                                   PATH=str(temp) + os.pathsep + os.environ["PATH"])
                        subprocess.run(["bash", "-eo", "pipefail", "-c", script],
                                       cwd=temp, env=env, check=True)
                        args = capture.read_text().splitlines()
                        self.assertIn(module["path"] + "/pom.xml", args)
                        self.assertIn("verify", args)
                        self.assertNotIn("deploy", args)
                        revisions = [arg for arg in args if arg.startswith("-Drevision=")]
                        if module["kind"] in {"thrift", "openapi"}:
                            self.assertEqual(len(revisions), 1)
                            self.assertRegex(revisions[0], r"^-Drevision=.+-SNAPSHOT$")
                        else:
                            self.assertEqual(revisions, [])
                        if profile:
                            self.assertIn("-P" + profile, args)
