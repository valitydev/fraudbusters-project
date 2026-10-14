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

Source checkout directories at the workspace root are excluded from the monorepo
index. Do not modify them; imported code is under the directories above.
