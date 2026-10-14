import importlib.util
from pathlib import Path
import unittest
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
