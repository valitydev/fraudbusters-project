# Повторная сверка миграции — 14 октября 2026

Сверка выполнена с зафиксированными SHA из `sources.json`, включая текущие локальные
изменения. Новые master исходных репозиториев не подменяли проверяемую базу.
Compose-окружение и приложения вместе не запускались. Сборки выполнялись во
временной копии; backend-тесты использовали свои изолированные fixtures.

## Что совпадает

- Все 20 subtree-импортов имеют исходное дерево Git и содержат исходные коммиты
  в истории. Проверка `scripts/verify-imports.py` прошла.
- Из 2431 исходного файла/ссылки 2377 совпадают по содержимому и executable mode,
  22 перенесены без изменения содержимого, один gitlink сохранён на исходном SHA.
- Производственный код семи из восьми backend-сервисов совпадает с исходниками.
  Единственное исключение в Java — два DAO trusted-tokens-manager, описанные ниже.
- Не изменены 202 файла контрактов, 32 миграции БД, 10 остальных backend runtime
  resources и 58 файлов `src/main` Fraudo.
- В 17 POM сохранены Maven coordinates, parent, dependencies/dependencyManagement
  и все существовавшие корневые properties, включая версии и настройки Java.
  Backend POM целиком идентичны исходным. UI package.json и lockfile не изменены.

Полный перечень: [source-parity.json](source-parity.json). Повторить сверку:

```bash
python3 scripts/verify-imports.py
python3 scripts/audit-source-parity.py --output /tmp/fraudbusters-source-parity.json
```

## Осознанные отличия

Текущий репозиторий не полностью побайтово идентичен импортированным деревьям:
29 исходных файлов адаптированы, два удалены. Новые файлы перечислены в JSON.

| Область | Отличие и проверка |
|---|---|
| trusted-tokens-manager | `TokenDaoImpl` и `TemplateDaoImpl` используют явное соответствие полей Row существующим колонкам. Имена таблиц/колонок, unique constraints, ключи upsert и JSON-десериализация сохранены. Добавлены реальные PostgreSQL-тесты insert/read/update обоих DAO. Это исправление поведения, а не только перемещение файлов. |
| UI | Тип формы notification template ID приведён к integer из management-контракта. Создание и редактирование проверяются через HttpTestingController: тот же POST `/notifications`, числовой templateId, сохранённый id при редактировании. |
| Генератор UI | Сохранён Swagger Codegen 3.0.57; добавлен mapping `Error=ModelError` и пропуск отсутствующего api.module.ts при генерации только моделей. Содержимое прежней ModelError-схемы и текущей Error-схемы одинаково. |
| Источник контракта UI | Старая JSON-копия удалена из Git; генерируется из неизменённого management OpenAPI. Backend уже использует опубликованный `1.160-e326aa8` того же контракта. В старой копии были DELETE вместо POST для удаления CSV и строковый templateId; HTTP-клиент UI уже использует POST. Добавленные template/rule query parameters необязательны. |
| UI Docker | nginx.conf помещён в стандартный `/etc/nginx/conf.d/default.conf`; сохранены исходные nginx.conf, runtime image и предусмотренный конфигурацией порт 8080. Добавлен минимальный Docker context. |
| UI-тесты | Настройки zone.js, обнаружение тестов и scaffold assertions приведены к существующим Angular/компонентам без обновления зависимостей. |
| Thrift POM | Только default commit.number=0 для локальной сборки; release передаёт номер явно. Classifier, relocation и генераторы сохранены. |
| OpenAPI POM | Bundling включён в Maven lifecycle, inputSpec разрешается относительно модуля. Server/client получают разные каталоги generated sources/classes; исходные генераторы и их настройки типов сохранены. |
| Fraudo/docs | Девять файлов документации Fraudo перенесены побайтово; Makefile/README указывают новый путь. Исправлена запись requirements основного сайта. |
| Local infra/E2E | Перенесены Compose и тесты, адаптированы local dependencies/configurations и fixtures. Новая ClickHouse initialization дополняет локальную legacy-схему полями существующих application migrations. Production migrations не изменены. |
| Удалённые файлы | Только `.DS_Store` management и генерируемая JSON-копия UI. Исходное содержимое доступно в импортированных Git-коммитах. |

## Проверки без Compose

Все отдельные `mvn -f <module>/pom.xml verify` завершились успешно.

| Модуль | Тестов | Failures/errors | Skipped |
|---|---:|---:|---:|
| fraudbusters | 201 | 0/0 | 0 |
| fraudbusters-api | 19 | 0/0 | 0 |
| fraudbusters-management | 140 | 0/0 | 0 |
| fraudbusters-mg-connector | 9 | 0/0 | 1 |
| fraudbusters-notificator | 48 | 0/0 | 0 |
| fraudbusters-warehouse | 6 | 0/0 | 0 |
| trusted-tokens-manager | 23 | 0/0 | 0 |
| wb-list-manager | 12 | 0/0 | 0 |
| fraudo | 53 | 0/0 | 0 |

- Восемь backend-сервисов: 458 тестов, failures/errors 0, skipped 1.
  Отключённый в исходниках `mg-connector/IntegrationTest` требует внешнего контура.
- Fraudo: 53 теста без failures/errors/skipped.
- Шесть Thrift-пакетов: `verify` прошёл.
- Два OpenAPI-пакета: `verify` прошёл отдельно для server и client;
  последовательное переключение профилей выполнялось без clean.
- UI: установка из неизменённого lockfile, OpenAPI lint, codegen, ESLint/Prettier,
  production build, 10 ChromeHeadless-тестов прошли. Новые notification tests также
  проверены TypeScript compiler и lint после окончательной правки импортов.
- CI scripts: 19 тестов выбора модулей, diff для PR/push, image packaging и
  публикации прошли. Публикация не выполнялась.
- Все восемь готовых Java JAR упакованы существующим helper в Docker contexts;
  исходные сгенерированные Dockerfile и их COPY inputs сохранены. Docker images
  повторно не собирались и не запускались.
- `docker compose config --quiet` прошёл для apps/e2e с обычной и ARM конфигурацией;
  `node --check` readiness script и `git diff --check` прошли.

Машиночитаемые результаты и названия test suites:
[compatibility-checks.json](compatibility-checks.json).

Локально использованы JDK 25 для приложений/Fraudo, JDK 21 для протоколов,
Node.js 23 для UI и Thrift 0.20.0. Исходные compiler/release settings и версии
CI не менялись. Повторная проверка с CI Thrift 0.19.0 и Erlang OTP 24/rebar3
в этот запуск не входила.

## Границы результата

Совпадение исходников, схем и настроек плюс тесты подтверждают проверенные сценарии
и отсутствие обнаруженных регрессий переноса. Они не гарантируют каждый возможный
production-сценарий. Сквозное взаимодействие всех приложений, реальные внешние
provider/notification endpoints, registry publication и staging/production deployment
в этой сверке не выполнялись. Последние healthcheck/readiness изменения Compose
проверены только через `docker compose config`; cold start не проверялся.

Проверялись зафиксированные исходники, а не локальные изменения старых checkout.
Эти checkout сохранены отдельно в `../fraudbusters-project-archive`.
