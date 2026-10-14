# Repository guidance

Deployable applications belong in `apps`, contracts in `packages/protocols`, Fraudo
in `packages/libs`, cross-service tests in `tests`, local infrastructure in `infra`.
Database migrations remain in the application that owns the database.

Preserve Maven coordinates, Docker image names, REST/Thrift contracts, Kafka topics
and storage compatibility during structural changes. Do not upgrade runtimes or
parent POMs as part of migration fixes. Do not edit generated Java/TypeScript files;
change their source contract or generator.

Applications use published dependencies. Verify a service independently with
`mvn -f apps/<service>/pom.xml verify`. Verify shared changes with `mvn verify`,
`python3 -m unittest discover -s tests/ci -v`, and the root npm scripts. For Thrift
pass `-Dpath_to_thrift=thrift`. Never store or print credentials.

Original source checkouts are archived outside this repository in the sibling
`fraudbusters-project-archive` directory. Work on imported code under the directories above.

Before changing code, refresh and install shared rules:

```bash
git submodule update --init --remote vendor/code-generation-rules
./.agent-rules/install.sh
```

<!-- BEGIN agent-rules -->
# Shared agent guidance

Treat current repository code, tests, build configuration, and service-specific
instructions outside this managed block as the primary evidence for this project's
architecture, tooling, and implementation patterns.

Keep these organization-level invariants:

- Do not edit generated sources. Change their source contract or generator instead.
- Preserve published wire/storage compatibility unless the task explicitly requires a
  coordinated breaking change or migration.
- Never hardcode, expose, or log credentials, tokens, personal data, PANs,
  bank-account data, or other payment-sensitive values.
- Prefer an established local implementation pattern over introducing a new framework,
  package layout, abstraction, or dependency without a concrete need.
- Do not promote an implementation choice observed in one service into an
  organization-wide rule without stronger repository or contract evidence.
- Run the project's existing focused tests, linters, generators, and compatibility
  checks that cover the changed area.
- Before reporting completion of a code or configuration change, review the final diff
  for unintended behavior changes, unrelated edits, missed mappings, and generated
  churn relevant to the changed area.
- Do not load or apply detailed guidance that is unrelated to the current task.

Detailed references are defaults and checklists, not permission to override stronger
repository evidence or explicit task requirements.

## Load targeted guidance only when relevant

- Database/schema/repository/transaction work: read `.agent-rules/references/database.md`.
- Generated-source or Protobuf work: read `.agent-rules/references/code-generation.md`.
- OpenAPI contract/generation work: read `.agent-rules/references/openapi.md`.
- Cross-layer architecture/client/converter decisions not settled by local code: consult
  `.agent-rules/references/code-conventions.md`.
- When a task changes an external provider boundary or provider flow — request/response,
  authentication, callback, polling, provider error mapping, provider integration tests,
  or adaptation of another provider — read `.agent-rules/skills/provider-adapter/SKILL.md`
  and follow its progressive-disclosure workflow. Do not load that skill for ordinary
  internal bugs or refactors that do not cross the provider boundary.
<!-- END agent-rules -->
