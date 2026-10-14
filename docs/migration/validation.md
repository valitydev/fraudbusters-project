# Проверка миграции — 14 октября 2026

Монорепозиторий: `fraudbusters-project`. Проверки выполнены локально на macOS
Apple Silicon. Исходные деревья и SHA находятся в `sources.json`.

| Проверка | Результат |
|---|---|
| Импорт Git-истории | 20 источников; деревья импортов совпадают с master, исходные коммиты доступны |
| Корневой Maven-агрегатор | Все 18 reactor-модулей успешно упакованы; финальный общий проход с `-DskipTests` |
| Тесты восьми Java-сервисов | 456 тестов: без failures/errors, один skipped; выполнены до исправления DAO |
| Fraudo | 53 теста прошли |
| Шесть Thrift-контрактов | Проверены; для локальной упаковки задан default `commit.number=0`, release задаёт номер явно |
| OpenAPI | Оба server-артефакта и оба client-артефакта собраны; переключение server/client без очистки проверено |
| OpenAPI lint | Оба контракта валидны |
| UI | Codegen, production build, Prettier и ESLint прошли; 8 unit-тестов прошли |
| CI scripts | 19 тестов прошли: выбор модулей, multi-commit push, rename/delete, упаковка JAR/agent, registry errors, стабильность версий |
| DAO PostgreSQL | 2 новых теста прошли: insert/read/upsert токенов и condition templates на существующей схеме |
| Docker | Собраны 9 приложений и E2E image; исходные Java runtime Dockerfile и OpenTelemetry agent сохранены |
| Compose | Валидны core/apps/e2e и ARM overrides |
| E2E | Все 35 проверок прошли; правила, привязки, UNIQUE/COUNT/SUM, геолокация, trusted tokens, белые/серые/чёрные списки |
| Документация | Основной сайт и Fraudo собраны MkDocs |
| Maven Central preflight | `--check` проверил новую версию fraudbusters-proto; публикация не выполнялась |
| Cleanup | Проверки остановлены по просьбе пользователя; артефакты сборок и тестового Docker-контура удалены, исходные checkout вынесены в соседний архив |

## Исправления, необходимые для проверки

- Swagger Codegen сохранён в версии 3.0.57; `Error` явно отображается в `ModelError`.
  Форма notification использует числовой template ID из текущего контракта.
- Обновлены только конфигурация и существующие assertions UI-тестов для Angular 17.
- Для Thrift добавлен безопасный локальный номер classifier; опубликованные release
  numbers по-прежнему задаются процессом выпуска.
- Разделены каталоги generated sources и classes OpenAPI server/client.
- Локальный Compose адаптирован к Boot 4, PostgreSQL-зависимостям и текущей схеме
  ClickHouse. Security exclusions и ленивый запуск внешнего Telegram/Vault относятся
  только к локальному Compose. Production-конфигурация не менялась.
- Старые E2E fixtures приведены к текущему контракту: ISO date-time, уникальные
  payment IDs, финальный captured для агрегатов, пустой массив validation errors.
- В текущем master trusted-tokens-manager обнаружен неверный reflection mapping:
  Row.key/value не соответствовали колонкам token/data и template_name/template_condition.
  Исправлены явные insert/read mappings; schema и JSON-формат хранения сохранены.
  Исправление выделено в отдельный коммит и покрыто PostgreSQL-тестами.

## Что ещё требует внешнего окружения

GitHub CLI не авторизован. Новый GitHub remote, secrets/branch protection,
реальная публикация Java/npm-пакетов и images, staging/production cutover и архивирование
исходных репозиториев не выполнялись. Следовать `README.md` этого каталога после
подключения учётной записи и проверки доступа к существующим пакетам.

Локальная Java-генерация использовала установленный Thrift 0.20.0; CI сохраняет
исходный 0.19.0. Erlang OTP 24/rebar3 и реальное выполнение GitHub Actions локально
не проверялись. Их исходные версии сохранены в `scripts/modules.json`.

Сквозные сценарии notifications/warehouse/crawler отдельно не добавлялись:
для них выполнены существующие тесты owning modules. Внешняя доставка уведомлений
и live mg-connector требуют тестового платёжного контура и инфраструктурных credentials.

## Остановленная финальная проверка

Повторный запуск Compose обнаружил смешение snapshot и transaction log ZooKeeper
в одном volume. Каталоги data/log разделены, добавлены readiness/healthcheck
зависимости и ожидание основного сервиса в E2E. Проверка этих последних изменений
не завершена по просьбе пользователя; результат 35 E2E относится к предыдущему
успешному запуску. Новые проверки после очистки не запускались.
