# Migration runbook

Local results: [validation report](validation.md).

## Source baseline

`sources.json` records the remote master SHA and source tree for each of the 20
imports. `python3 scripts/verify-imports.py` verifies tree equality at each import
commit and reachability of the original history. Original local checkouts were moved intact to the sibling
`fraudbusters-project-archive` directory.

The eight Java services use JDK 25 on the fetched master. Fraudo compiles for Java
17; protocol compiler settings and parent POMs are unchanged. The OpenAPI source
CI uses Node 16/JDK 21, UI uses Node 23. `scripts/modules.json` defines the CI matrix.

## GitHub setup

The public repository is `https://github.com/valitydev/fraudbusters-project`, default
branch `master`. Configure the following before the first release:

1. Require `Required checks` and `Publication checks` in branch protection.
2. In every existing GHCR package, grant **Manage Actions access → fraudbusters-project → Write**.
   If using a PAT instead, configure `GHCR_PAT` with `write:packages`; set repository
   variable `GHCR_USERNAME` to the token owner's login. Otherwise GITHUB_TOKEN uses
   the workflow actor. Package names remain unchanged and must allow public pulls.
3. Configure `CENTRAL_USERNAME` and `CENTRAL_TOKEN` from a **Central Portal user token**,
   plus `MAVEN_GPG_PRIVATE_KEY` and `MAVEN_GPG_PASSPHRASE`. The signing key must be the
   one approved for publishing. Legacy OSSRH_* secret names remain supported as
   fallbacks; their credentials must also be Portal credentials, not retired OSSRH tokens.
4. Run `Fraudbusters CI` manually on `master`, target `all`, for one complete baseline
   of nine image tags and all missing package releases. Alternatively run `images`
   and `contracts` separately. A single IMAGE_TAG for all local apps requires a complete
   image baseline; ordinary selective releases may use different last-built SHA tags.
5. Confirm **Publication checks** succeeded and the emitted SHA tags/digests and
   Maven artifacts/signatures can be fetched. Re-running skips immutable versions/tags.

All package releases use native Central Publishing, with server-id `ossrh` retained
for settings compatibility. Five older protocol POMs have an additional `central-release`
profile disabling the inherited Nexus extension and adding Central 0.8.0, GPG 3.2.8
and flatten 1.7.1. Existing parent POMs, Java/compiler settings, source generators,
coordinates, classifiers and relocation rules remain unchanged. The publication helper
activates the extra profile automatically; use it instead of bare `mvn -Pdeploy`.
Maven 3.9.12 is installed with SHA-512 verification to avoid changes in hosted-runner
Maven defaults affecting the existing Central plugin. No service runtime was upgraded.

## Validation gates

Run the focused CI tests, import verification, full Maven reactor, each OpenAPI
client profile, UI codegen/check/build/tests, and Compose configuration checks.
Java tests requiring Docker must run on a host with a functioning Docker daemon.

```bash
python3 -m unittest discover -s tests/ci -v
python3 scripts/verify-imports.py
mvn -B --fail-at-end verify -Dpath_to_thrift=thrift
mvn -B -f packages/protocols/swag-fraudbusters/pom.xml -Pclient verify
mvn -B -f packages/protocols/swag-fraudbusters-management/pom.xml -Pclient verify
npm run ui:install
npm run ui:codegen
npm run ui:check
npm run ui:test
npm run ui:build
scripts/build-local-images.sh
npm run e2e
```

For ARM, pass both Compose files. Third-party legacy images without ARM variants
use `linux/amd64` emulation. The upstream Java runtime images determine whether
application images can be built natively; CI retains the original amd64 release
platform until native ARM runtime support is verified.

The existing E2E suite covers templates/references, payment inspection, aggregates,
geolocation, trusted tokens and white/black lists. Notification, warehouse and
crawler behavior additionally rely on the owning modules' existing tests; these
are not claimed as new cross-service scenarios. mg-connector requires the external
payment contour to validate live ingestion; local defaults disable that ingestion.

## Release verification

Use Actions manual target `contracts` to check and publish missing immutable Java
versions. Server/client OpenAPI artifacts publish independently within each module.
Repeat the run to verify existing versions are skipped. `--check` reads public
POM/JAR/classifier availability; `--prepare` runs release verify without signing or upload:

```bash
python3 scripts/publication.py packages/protocols/fraudbusters-proto --check
python3 scripts/publication.py packages/protocols/fraudbusters-proto --prepare
python3 scripts/publication.py packages/protocols/swag-fraudbusters --prepare --profile client
```

Registry authentication failures, HTTP 429/5xx and transport errors fail the
preflight rather than trigger a publish. Partial existing releases are not overwritten.
After deploy, the helper waits for main artifacts, sources/javadoc, classifiers and
ASCII signatures to become readable from Central. GPG passphrases are passed only
through the environment, never as process arguments. Test that a separate consumer can resolve
its explicitly selected release before updating an application dependency.

Fraudo uses a fixed POM version: raise it explicitly before releasing changed code.
For generated versioned contracts, the source commit count is extended by commits
of that module on the monorepo first-parent history. Unrelated commits do not
create new contract versions. Classifier/relocation receive the same release count.

The two Erlang protocols retain compile checks. No npm protocol package is
configured in the imported master; their historical frontend workflows were
conditional and did not publish an absent package.

## Cutover and rollback

Before switching writers, freeze source changes briefly and compare each live
remote master with `sources.json`. Integrate any subsequent commits into the
corresponding subtree, verify again and update the baseline record intentionally.

After every validation gate passes:

1. Publish images from the new repository and record their immutable digests.
2. Test those images in the existing staging environment, including integrations
   with the real payment contour and notification destinations.
3. Update the existing deployment repository's image tags/digests while preserving
   service names, ingress routes, Kafka topics and database ownership.
4. Disable the old source repositories' release pipelines before enabling a single
   production release writer. Keep old documentation sites until their consumers
   have been redirected to the new documentation site.
5. Only after independent consumers and production checks pass, make the original
   repositories read-only/archive them.

For rollback, restore the recorded previous image digests in the deployment
repository. This migration introduces no database or wire-format migrations.
Do not delete previous package releases or images.
