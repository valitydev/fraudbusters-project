#!/usr/bin/env python3
"""Read public GHCR manifests so retries never replace an existing SHA tag."""
import argparse
import json
import re
import time
import urllib.error
import urllib.parse
import urllib.request

ACCEPT = ", ".join([
    "application/vnd.oci.image.index.v1+json",
    "application/vnd.docker.distribution.manifest.list.v2+json",
    "application/vnd.oci.image.manifest.v1+json",
    "application/vnd.docker.distribution.manifest.v2+json",
])


def manifest_digest(image, required_platforms=()):
    match = re.fullmatch(r"ghcr\.io/([a-z0-9_.-]+/[a-z0-9_.-]+):(sha-[a-f0-9]{7,40})", image)
    if not match:
        raise ValueError("Expected ghcr.io/<owner>/<app>:sha-<commit>")
    repository, tag = match.groups()
    query = urllib.parse.urlencode({"service": "ghcr.io", "scope": "repository:" + repository + ":pull"})
    with urllib.request.urlopen("https://ghcr.io/token?" + query, timeout=30) as response:
        token = json.load(response)["token"]
    request = urllib.request.Request("https://ghcr.io/v2/" + repository + "/manifests/" + tag,
                                     headers={"Accept": ACCEPT, "Authorization": "Bearer " + token},
                                     method="GET" if required_platforms else "HEAD")
    try:
        with urllib.request.urlopen(request, timeout=30) as response:
            digest = response.headers.get("Docker-Content-Digest")
            if not digest or not re.fullmatch(r"sha256:[a-f0-9]{64}", digest):
                raise RuntimeError("GHCR did not return a valid immutable manifest digest")
            if required_platforms:
                manifest = json.load(response)
                platforms = {
                    platform.get("os", "") + "/" + platform.get("architecture", "")
                    for entry in manifest.get("manifests", [])
                    for platform in [entry.get("platform", {})]
                }
                missing = set(required_platforms) - platforms
                if missing:
                    raise RuntimeError("Published image lacks required platforms: " + ", ".join(sorted(missing))
                                       + ". Publish a new commit tag; existing SHA tags are immutable.")
            return digest
    except urllib.error.HTTPError as error:
        if error.code == 404:
            return None
        raise


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("image")
    parser.add_argument("--require", action="store_true", help="Fail unless the image is publicly readable")
    parser.add_argument("--github-output", action="store_true")
    parser.add_argument("--expected-digest", default="")
    parser.add_argument("--platform", action="append", default=[],
                        help="Require a platform in the image index (repeat for each platform)")
    args = parser.parse_args()
    def check_manifest():
        return manifest_digest(args.image, args.platform) if args.platform else manifest_digest(args.image)

    digest = check_manifest()
    if args.require:
        for _ in range(15):
            if digest:
                break
            time.sleep(2)
            digest = check_manifest()
    if args.require and not digest:
        raise RuntimeError("Published SHA image is not publicly readable: " + args.image)
    if args.expected_digest and digest != args.expected_digest:
        raise RuntimeError("Public GHCR manifest does not match the pushed image digest")
    if args.github_output:
        print("exists=" + ("true" if digest else "false"))
        print("digest=" + (digest or ""))
    else:
        print(args.image + ": " + (digest or "not yet published"))


if __name__ == "__main__":
    main()
