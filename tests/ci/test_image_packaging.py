from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
import zipfile

SCRIPT = Path(__file__).resolve().parents[2] / "scripts/package_java_image.py"


class ImagePackagingTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.target = self.root / "apps/example/target"
        self.target.mkdir(parents=True)
        with zipfile.ZipFile(self.target / "app.jar", "w") as jar:
            jar.writestr("BOOT-INF/classes/", "")
        agent = self.target / "maven-shared-archive-resources/agent.jar"
        agent.parent.mkdir()
        with zipfile.ZipFile(agent, "w") as jar:
            jar.writestr("agent.class", "")
        (self.target / "Dockerfile").write_text("FROM upstream-runtime\nCOPY --chown=vality:vality app.jar /opt/app.jar\nCOPY --chown=vality:vality maven-shared-archive-resources/agent.jar /opt/agent.jar\n")

    def tearDown(self):
        self.temp.cleanup()

    def run_packaging(self):
        return subprocess.run([sys.executable, str(SCRIPT), "example", "output"], cwd=self.root, capture_output=True, text=True)

    def test_copies_upstream_runtime_agent_and_boot_jar(self):
        result = self.run_packaging()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertTrue((self.root / "output/app.jar").is_file())
        self.assertTrue((self.root / "output/maven-shared-archive-resources/agent.jar").is_file())
        self.assertEqual((self.root / "output/Dockerfile").read_bytes(), (self.target / "Dockerfile").read_bytes())

    def test_missing_agent_fails_before_creating_output(self):
        (self.target / "maven-shared-archive-resources/agent.jar").unlink()
        self.assertNotEqual(self.run_packaging().returncode, 0)
        self.assertFalse((self.root / "output").exists())

    def test_path_traversal_is_rejected(self):
        (self.target / "Dockerfile").write_text("COPY ../secret /opt/secret\n")
        self.assertNotEqual(self.run_packaging().returncode, 0)
        self.assertFalse((self.root / "output").exists())
