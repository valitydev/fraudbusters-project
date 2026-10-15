# Проверка исправления публикации — 15 октября 2026

Исправлены условия release jobs после skipped-зависимостей. Jobs явно требуют
успеха affected/checks и default branch; PR и другие ветки ничего не публикуют.
Новый итоговый `Publication checks` завершится ошибкой при пропуске/ошибке
выбранной публикации, даже если сборочный `Required checks` успешен.

Пять старых Thrift POM получили только release-профиль `central-release`:
он отключает Nexus extension, включает native Central 0.8.0, GPG 3.2.8 и flatten
1.7.1. Это те же publisher/signing/flatten версии, которые используются современными
parent POM проекта. Сохранены все исходные parent coordinates, зависимости,
версии Java, генераторы, схемы, classifier и правила relocation.

Maven в CI закреплён на проверенном 3.9.12: более новые Maven могут менять
валидацию snapshot-моделей и содержимое bundle для Central plugin 0.8.0.
Публикация использует Portal tokens, поддерживая старые имена secrets.

## Результаты

- 42 теста CI-скриптов прошёл: выбор модулей, фактическая команда Java-матрицы,
  release-профили, immutable versions, ошибки registry, gate результатов публикации,
  публичные SHA manifests/digests, отсутствие signing secrets в argv.
- Реальный Maven `verify` release-профиля выполнен во временной копии для всех
  девяти Maven-модулей: 11 вариантов с отдельными OpenAPI server/client.
- Для каждого варианта проверены основной JAR, sources, javadoc и resolved version
  в flattened POM. Локальный build.finalName сохраняется и может не включать версию;
  внешние Maven filenames определяются GAV.
- Для шести Thrift classifiers проверены имена и содержимое JAR: main classes
  присутствуют с применением исходных relocation rules. Существующие no-op mappings
  для других namespaces также сохранены.
- Effective POM всех девяти модулей подтверждает: Central extension 0.8.0 активен,
  старый Nexus отсутствует или extensions=false; GPG 3.2.8 активен.
- На старом notificator parent реально сформированы и проверены пять GPG-подписей:
  основной JAR, POM, sources, javadoc и classifier. Maven прочитал passphrase из env.
  Использован одноразовый ключ в отдельном GNUPGHOME, удалённый после проверки;
  пользовательские ключи не использовались. Подписанный POM содержит resolved version.
- Установка Maven 3.9.12 из Central и проверка SHA-512 реально выполнены во временной папке.
- GHCR helper прочитал публичный digest существующего исходного SHA-тега;
  для отсутствующего monorepo SHA он вернул exists=false без попытки публикации.
- Actionlint 1.7.12 проверил workflow и GitHub expressions без ошибок; YAML, bash syntax
  и git diff --check прошли. Сверка исходников
  подтверждает неизменность контрактов, runtime resources, миграций и Maven metadata.

Проверка упаковки выполнена локально на macOS/JDK 21; CI сохраняет JDK 17 для
Thrift/Fraudo, JDK 21 для OpenAPI и JDK 25 для приложений. Дополнительный release verify
включён в соответствующие CI matrix jobs.

## Что требует GitHub environment

Upload в GHCR/Central, изменение secrets/grants и запуск production deployment
в этой проверке не выполнялись. Для первого настоящего выпуска нужны Portal token,
GPG key и GHCR Actions write access, описанные в [runbook](README.md).
После push в master запустите `Fraudbusters CI`, target `all`, и проверьте
`Publication checks` вместе с реальными registry artifacts. Existing versions и
SHA tags при повторном запуске не перезаписываются.

Machine-readable results: [publication-fix-validation.json](publication-fix-validation.json).
