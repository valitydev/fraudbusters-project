import contextlib
import importlib.util
import io
import json
from pathlib import Path
import unittest
from unittest.mock import MagicMock, patch
import urllib.error

spec = importlib.util.spec_from_file_location("images", Path(__file__).resolve().parents[2] / "scripts/image-publication.py")
images = importlib.util.module_from_spec(spec)
spec.loader.exec_module(images)
IMAGE = "ghcr.io/valitydev/fraudbusters:sha-1234567"
DIGEST = "sha256:" + "a" * 64


class ImagePublicationTests(unittest.TestCase):
    def token(self):
        return io.BytesIO(json.dumps({"token": "anonymous-test-token"}).encode())

    def test_existing_tag_returns_digest_for_an_idempotent_retry(self):
        response = MagicMock()
        response.__enter__.return_value.headers = {"Docker-Content-Digest": DIGEST}
        with patch.object(images.urllib.request, "urlopen", side_effect=[self.token(), response]) as request:
            self.assertEqual(images.manifest_digest(IMAGE), DIGEST)
        self.assertEqual(request.call_args.args[0].method, "HEAD")

    def test_only_manifest_404_means_tag_is_missing(self):
        for code in [401, 403, 429, 500, 503]:
            error = urllib.error.HTTPError("https://registry.invalid", code, "error", {}, None)
            with patch.object(images.urllib.request, "urlopen", side_effect=[self.token(), error]):
                with self.assertRaises(urllib.error.HTTPError):
                    images.manifest_digest(IMAGE)
        error = urllib.error.HTTPError("https://registry.invalid", 404, "missing", {}, None)
        with patch.object(images.urllib.request, "urlopen", side_effect=[self.token(), error]):
            self.assertIsNone(images.manifest_digest(IMAGE))

    def test_registry_token_errors_are_not_treated_as_missing_images(self):
        error = urllib.error.HTTPError("https://registry.invalid/token", 403, "denied", {}, None)
        with patch.object(images.urllib.request, "urlopen", side_effect=error):
            with self.assertRaises(urllib.error.HTTPError):
                images.manifest_digest(IMAGE)

    def test_network_failure_does_not_allow_overwriting_a_tag(self):
        with patch.object(images.urllib.request, "urlopen", side_effect=urllib.error.URLError("offline")):
            with self.assertRaises(urllib.error.URLError):
                images.manifest_digest(IMAGE)

    def test_github_outputs_and_public_digest_confirmation(self):
        output = io.StringIO()
        with patch("sys.argv", ["images", IMAGE, "--github-output"]), \
                patch.object(images, "manifest_digest", return_value=DIGEST), contextlib.redirect_stdout(output):
            images.main()
        self.assertEqual(output.getvalue(), "exists=true\ndigest=" + DIGEST + "\n")
        with patch("sys.argv", ["images", IMAGE, "--require", "--expected-digest", "sha256:" + "b" * 64]), \
                patch.object(images, "manifest_digest", return_value=DIGEST):
            with self.assertRaisesRegex(RuntimeError, "does not match"):
                images.main()

    def test_missing_public_image_fails_post_publication_check(self):
        with patch("sys.argv", ["images", IMAGE, "--require"]), \
                patch.object(images, "manifest_digest", return_value=None), patch.object(images.time, "sleep"):
            with self.assertRaisesRegex(RuntimeError, "not publicly readable"):
                images.main()
