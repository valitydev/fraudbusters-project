# fraudbusters-project

Монорепозиторий Fraudbusters: инспекция платежей, управление антифрод-правилами
и списками, обработка событий, аналитическое хранилище, уведомления и UI.

## Структура

- `apps/` — восемь Java-сервисов и `fraudbusters-ui`.
- `packages/protocols/` — шесть Thrift-контрактов и два Swagger/OpenAPI-контракта.
- `packages/libs/fraudo/` — язык антифрод-правил.
- `infra/` — локальные зависимости и конфигурация Docker Compose.
- `tests/e2e/` — существующие сквозные сценарии инспекции, агрегатов, геолокации,
  trusted tokens и белых/чёрных списков.
- `tests/ci/` — проверки выбора модулей и безопасной публикации.
- `docs/` — документация продукта, Fraudo и миграции.

Crawler-функциональность реализована в `fraudbusters-management`;
`fraud-data-crawler-proto` описывает её контракт, отдельного сервиса нет.

## Исходники и история

Импортированы свежие `origin/master` 20 исходных репозиториев через Git subtree
без squash. SHA, деревья исходников и инструменты сборки записаны в
`docs/migration/sources.json` и `scripts/modules.json`. Исходные коммиты доступны
в истории нового репозитория; исходные теги имеют префикс имени компонента.

```bash
python3 scripts/verify-imports.py
```

Команда проверяет исходные деревья в импортирующих коммитах и наличие исходной
истории. Локальные рабочие изменения и коммиты вне удалённого master не переносились.
Старые checkout в корне этой рабочей папки исключены из индекса; при обычном
клонировании нового репозитория они отсутствуют.

## Требования и сборка

Нужны JDK 25, Maven 3, Node.js 23 для UI, Docker с Compose и Thrift 0.19.0
для Java-контрактов. OpenAPI-модули сохраняют Node.js 16 из исходного CI:
Maven самостоятельно устанавливает его в `target/frontend`. Компиляционные
настройки, parent POM и версии библиотек приложений сохранены.

```bash
mvn -B verify -Dpath_to_thrift=thrift
mvn -B -f apps/fraudbusters-api/pom.xml verify
mvn -B -pl apps/fraudbusters-management -am verify
python3 -m unittest discover -s tests/ci -v
```

Полная сборка OpenAPI по умолчанию генерирует server-артефакты; client проверяется
отдельно с `-Pclient`. Maven автоматически устанавливает npm-зависимости и
собирает bundle контракта до Java codegen.

```bash
npm run openapi:install
npm run openapi:validate
npm run ui:install
npm run ui:codegen
npm run ui:check
npm run ui:test
npm run ui:build
```

Существующий Swagger Codegen 3.0.57 генерирует TypeScript-модели из текущего
management OpenAPI. JSON-копия контракта и генерируемые модели не хранятся в Git.

## Контракты и публикация

В CI приложения используют **явно опубликованные версии** из своих POM:
каждый сервис собирается отдельно через `-f apps/<service>/pom.xml`.
Изменение контракта или Fraudo не подменяет зависимости в CI. Локальный корневой
reactor и `-am` допускают совместную проверку модулей с совпадающими версиями.
После выпуска пакета его потребители обновляются отдельным PR.

Java-контракты и Fraudo сохраняют Maven Central и исходные Maven coordinates.
Thrift сохраняет генераторы, classifiers и relocation. Новый формат номера
остаётся `1.<commit-number>-<sha>`: исходный счётчик дополняется коммитами
конкретного модуля после импорта. Изменение другого сервиса не меняет релиз
контракта. Для Fraudo сохраняется явная версия из POM — её нужно поднимать
при выпуске нового содержимого.

```bash
python3 scripts/publication.py packages/protocols/fraudbusters-proto --check
```

`--check` только проверяет наличие версии. Публикация пропускает существующие
релизы, а ошибки доступа или сети останавливают её. Старые npm-workflows
протоколов были условными: в импортированном master нет npm-пакетов для выпуска.
Erlang-проверки сохранены для двух протоколов с `rebar.config`.

## Локальный запуск

```bash
docker compose -f infra/compose.yaml --profile core up -d
scripts/build-local-images.sh
docker compose -f infra/compose.yaml --profile apps up --build -d
npm run e2e
```

Java-образы собираются из Dockerfile, полученного исходным Maven-процессом,
включая OpenTelemetry agent. Локальные имена — `ghcr.io/valitydev/<app>:local`.
Для опубликованного набора можно задать `IMAGE_TAG=sha-<sha>` и запускать без
`--build`. Сквозные тесты используют отдельный Compose project
`fraudbusters-project-e2e`.

На Apple Silicon подключите исходные ARM overrides:

```bash
docker compose -f infra/compose.yaml -f infra/compose.arm.yaml --profile apps up --build -d
```

| Ресурс | Адрес |
|---|---|
| UI | http://localhost:8989 |
| Management | http://localhost:8085/fb-management/v1 |
| Inspector API | http://localhost:9999 |
| Fraudbusters Thrift | http://localhost:8999 |
| Kafka на хосте | localhost:29092 |
| ClickHouse HTTP | http://localhost:8123 |
| Grafana | http://localhost:3000 |
| Keycloak | http://localhost:8080/auth |

Compose сохраняет legacy-версии внешних Kafka, Riak, ClickHouse и Keycloak;
обновление этих компонентов не входит в структурную миграцию. Для mg-connector
внешние обращения отключены по умолчанию: для живого платёжного контура задайте
адреса `INVOICING_URL`, `WITHDRAWAL_URL`, `DOMINANT_URL`, `DESTINATION_URL` и включите
`fb.stream.invoiceEnabled`/`fb.stream.withdrawalEnabled` в override.

## CI и переключение

Корневой GitHub Actions проверяет изменённые модули; server/client OpenAPI
проверяются отдельно. Общие изменения сборки проверяют весь контур. PR diff
использует merge-base, push — SHA до/после события, включая несколько коммитов.
Обязательный итоговый check: `Required checks`.

Ручные режимы: `all`, `images`, `contracts`. После успешных проверок master
публикует отдельные образы с SHA-тегом и новые Java-пакеты. Имена образов
сохраняются. Для publication нужны исходные OSSRH/GPG secrets; для прежних GHCR
пакетов нужно предоставить новому репозиторию доступ либо настроить `GHCR_PAT`.

Порядок подключения GitHub, проверки публикации и переключения окружений:
[Migration runbook](docs/migration/README.md).
Проверенные сценарии и оставшиеся внешние шаги: [Validation report](docs/migration/validation.md).
