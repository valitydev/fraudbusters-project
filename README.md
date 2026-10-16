# Fraudbusters

[![CI](https://github.com/valitydev/fraudbusters-project/actions/workflows/ci.yml/badge.svg)](https://github.com/valitydev/fraudbusters-project/actions/workflows/ci.yml)

Fraudbusters is a platform for detecting and analyzing fraudulent payment activity.
It evaluates transactions against rules written in the Fraudo domain-specific
language and provides tools for managing rules, reviewing transaction data,
maintaining allowlists and blocklists, and configuring notifications.

This repository contains the backend services, web application, shared libraries,
and API contracts that make up the platform.

## Components

| Application | Responsibility |
| --- | --- |
| [fraudbusters](apps/fraudbusters) | Evaluates transactions against fraud detection rules. |
| [fraudbusters-api](apps/fraudbusters-api) | Exposes the payment inspection REST API. |
| [fraudbusters-management](apps/fraudbusters-management) | Manages rules, templates, bindings, lists, and data crawler operations. |
| [fraudbusters-mg-connector](apps/fraudbusters-mg-connector) | Converts payment processing events into the Fraudbusters event model. |
| [fraudbusters-notificator](apps/fraudbusters-notificator) | Manages notification rules, channels, and scheduled delivery. |
| [fraudbusters-warehouse](apps/fraudbusters-warehouse) | Provides access to transaction and fraud analytics in ClickHouse. |
| [trusted-tokens-manager](apps/trusted-tokens-manager) | Manages trusted payment tokens. |
| [wb-list-manager](apps/wb-list-manager) | Manages allowlists and blocklists. |
| [fraudbusters-ui](apps/fraudbusters-ui) | Provides the web interface for fraud management and analysis. |

[Fraudo](packages/libs/fraudo) defines the rule language. Shared Thrift and
OpenAPI definitions live in [packages/protocols](packages/protocols).
Services communicate through REST, Thrift, and Kafka.

## Build a service

For a backend build, install **JDK 25** and **Maven 3.9.12**. Run commands from the
repository root.

Build a single service using its published dependencies:

```bash
mvn -B -f apps/fraudbusters-api/pom.xml package -DskipTests
```

The application JAR and container resources are written to the service's `target/`
directory. Replace `fraudbusters-api` with another backend service as needed.
There is no need to build the shared packages first.

Build and run the service's tests:

```bash
mvn -B -f apps/fraudbusters-api/pom.xml verify
```

Integration tests in some services require Docker. The management and notificator
builds also start embedded PostgreSQL for schema generation; use `-Ddb.port=55431`
if the default port is occupied. See each application's README for runtime
configuration.

## Build the web application

Install **Node.js 23**, npm, and a JDK, then run:

```bash
npm run ui:install
npm run ui:build
```

The build bundles the management OpenAPI contract, generates the TypeScript client,
and builds the Angular application. Output is written to
`apps/fraudbusters-ui/dist/`.

To run the UI checks and tests:

```bash
npm run ui:check
npm run ui:test
```

Run the build before these commands to generate the API client. UI tests require
Chrome or Chromium; set `CHROME_BIN` if the executable is not detected automatically.

## Build all Java modules

In addition to the backend requirements, install **Apache Thrift 0.19.0** and make
`thrift` available on `PATH`:

```bash
mvn -B verify -Drevision=0-SNAPSHOT -Dpath_to_thrift=thrift
```

`revision` supplies a valid snapshot version for locally generated contract
artifacts. Applications use the dependency versions declared in their own POMs.
The root Maven build covers Java modules; build the UI separately with the commands
above.

When building packages independently, CI uses JDK 17 for Thrift contracts and Fraudo,
and JDK 21 for OpenAPI contracts. OpenAPI Maven builds provision Node.js 16 for their
contract tooling. OpenAPI Java generation defaults to the server profile; verify a
client explicitly with:

```bash
mvn -B -f packages/protocols/swag-fraudbusters-management/pom.xml \
  verify -Pclient -Drevision=0-SNAPSHOT
```

Validate the OpenAPI specifications and the CI tooling independently:

```bash
npm run openapi:install
npm run openapi:validate
npm run test:ci
```

## Local development

Docker Compose configuration, dependency initialization, and monitoring are in
[infra](infra). To build local images and start the application stack:

```bash
scripts/build-local-images.sh
docker compose -f infra/compose.yaml --profile apps up -d
```

The image script requires the backend and UI build tools listed above. It builds
all nine application images with the `local` tag. To build only selected backend
images, pass their names, for example:

```bash
scripts/build-local-images.sh fraudbusters fraudbusters-api
```

Compose profiles select the required environment:

| Profile | Purpose |
| --- | --- |
| `core` | Databases, messaging, authentication, and supporting infrastructure. |
| `apps` | Application services and their local dependencies. |
| `e2e` | Infrastructure and services used by the end-to-end test suite. |

On Apple Silicon, add `-f infra/compose.arm.yaml` after the main Compose file.

| Endpoint | Local address |
| --- | --- |
| Web application | http://localhost:8989 |
| Management API | http://localhost:8085/fb-management/v1 |
| Inspection API | http://localhost:9999 |
| Grafana | http://localhost:3000 |

Run cross-service scenarios with:

```bash
npm run e2e
```

External payment processing integrations require the appropriate service endpoints
and credentials in your environment. See the application configuration and
[local infrastructure guide](infra/README.md) before enabling them.

## Repository layout

```text
apps/                 Backend services and Angular web application
packages/libs/        Fraudo rule language
packages/protocols/   Thrift and OpenAPI contracts
infra/                Local infrastructure and monitoring
scripts/              Build, code generation, and CI tools
tests/                Cross-service tests and CI tooling tests
docs/                 Product and rule language documentation
```

## Documentation and contributions

- [Product documentation](docs/content/index.md)
- [Architecture](docs/content/docs/architecture.md)
- [API documentation](docs/content/docs/api.md)
- [Fraudo language reference](docs/fraudo/content/index.md)
- [Issue tracker](https://github.com/valitydev/fraudbusters-project/issues)

When contributing, run the checks for the affected modules. Change contract sources
or generators when updating API clients; generated Java and TypeScript files should
not be edited directly.

## License

Apache License 2.0. License files are included in each application and shared package;
see the [Fraudbusters license](apps/fraudbusters/LICENSE).
