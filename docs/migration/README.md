# Migration runbook

## Source baseline

`sources.json` records the remote master SHA and source tree for each of the 20
imports. `python3 scripts/verify-imports.py` verifies tree equality at each import
commit and reachability of the original history. Original local checkouts are
excluded from the root index and were not changed.

The eight Java services use JDK 25 on the fetched master. Fraudo compiles for Java
17; protocol compiler settings and parent POMs are unchanged. The OpenAPI source
CI uses Node 16/JDK 21, UI uses Node 23. `scripts/modules.json` defines the CI matrix.

## GitHub setup

The local repository is ready under the name `fraudbusters-project`. GitHub CLI
on the migration workstation is not authenticated; no remote repository was
created and no publication, deployment or source-repository archival was executed.

1. Create `fraudbusters-project` in the same organization as the existing services,
   with the intended visibility and `master` as its default branch.
2. Push the master history and namespaced source tags. Configure `origin` only
   for the new repository; the `source-*` remotes are provenance references.
3. Configure branch protection with required check `Required checks`.
4. Grant the new repository Actions access to all existing GHCR service packages.
   If package permissions cannot be inherited, configure `GHCR_PAT` with write
   access to those packages. Keep each image name unchanged.
5. Transfer `OSSRH_USERNAME`, `OSSRH_TOKEN`, `OSSRH_GPG_SECRET_KEY`,
   `OSSRH_GPG_SECRET_KEY_PASSWORD` through the organization's secret store.
   Credentials are never copied into repository files.
6. Verify read access to any private transitive dependencies; this checkout's
   dependencies currently use their original Maven repository configuration.

The imported older library parents use Sonatype OSSRH, newer parents use Central
Publishing. Both preserve their upstream release channels. If the old OSSRH
endpoint is no longer accepted for the account, resolve publishing configuration
with the package owner before enabling releases; do not silently change package
coordinates or overwrite a release.

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
Repeat the run to verify existing versions are skipped. `--check` is read-only:

```bash
python3 scripts/publication.py packages/protocols/fraudbusters-proto --check
```

Registry authentication failures, HTTP 429/5xx and transport errors fail the
preflight rather than trigger a publish. Test that a separate consumer can resolve
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
