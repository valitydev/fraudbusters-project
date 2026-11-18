# k6: функциональные и нагрузочные тесты Fraudbusters

Тесты проходят сквозные сценарии через публичные API: fraudbusters-management, fraudbusters-api и Thrift-инспектор
выплат. Нужны Docker и Node.js. Наборы, кейсы и соглашения описаны в [suites](suites/README.md).

## Быстрый старт

Один набор на одной версии:

```bash
scripts/run-k6.sh up <версия>
scripts/run-k6.sh test <версия> payments functional
scripts/run-k6.sh down
```

Две версии от smoke до нагрузки:

```bash
scripts/run-k6.sh series <эталон> <кандидат>
```

Стенд — отдельный Compose-проект `fb-regression` без проброса портов, поэтому рядом может работать обычный локальный
стек. Числа стенда годятся для сравнения версий между собой, но не говорят о ёмкости реальной среды.

## Структура

```
suites/<набор>/        main.js, cases/<ID>.js, steps.js, README.md с описанием кейсов
data/<набор>/<ID>.js   данные кейса: шаблоны правил, операции, ожидаемые решения
data/traffic.js        состав нагрузочного трафика
lib/ clients/ fixtures/ общий код: профили, проверки, ожидание, HTTP- и Thrift-клиенты, модели операций
versions/              версии стенда
tools/                 готовность стенда, отчёт прогона, сравнение двух прогонов
k8s/                   запуск в Kubernetes
Dockerfile             образ с тестами для запуска в кластере
reports/               отчёты сравнений
```

## Версии

Версия — файл `versions/<версия>.env`.

| Переменная | Значение |
| --- | --- |
| `*_TAG` | теги образов сервисов |
| `FEATURES` | функциональности версии; кейсы, которым нужна отсутствующая, получают `skipped` ([suites](suites/README.md#функциональности-и-версии)) |
| `INFRA_COMMIT` | коммит, `infra/` которого поднимает стенд (ClickHouse, Kafka, базы); без переменной — текущая `infra/` |

`INFRA_COMMIT` нужен, когда версии отличаются инфраструктурой: каждая из них запускается на своей.

Если образа нет в реестре, его можно собрать локально:

```bash
scripts/build-local-images.sh <сервис>
docker tag ghcr.io/valitydev/<сервис>:local ghcr.io/valitydev/<сервис>:<тег>
```

## Профили

| Профиль | Что запускает |
| --- | --- |
| `smoke` | кейсы с пометкой smoke |
| `functional` | все кейсы набора, по `CONCURRENCY` (6) одновременно |
| `baseline` | `RATE` итераций/с (10) в течение `DURATION` (5m) |
| `load` | разгон до `TARGET_RATE` (2×`RATE`), плато, смена шаблона на `RECONFIGURE_AT` (3m) |
| `stress` | ступени `RATE` × `STRESS_FACTORS` (1,2,3,4) |
| `spike` | всплеск до `SPIKE_RATE` (4×`RATE`) |
| `soak` | `RATE` в течение `DURATION` (1h) |

Отдельные кейсы: `K6_CASES=PAY-FL-19,PAY-FL-20`. Доли трафика задаются переменными `SHARE_<поток>`, описанными в
[data/traffic.js](data/traffic.js).

Проверка обновления на живых данных: `scripts/run-k6.sh lifecycle <версия> [<версия после обновления>]`. Обе версии
должны работать на одной инфраструктуре.

## Серия

```bash
scripts/run-k6.sh series <эталон> <кандидат> [<набор>:<профиль> ...]
```

**Шаги по умолчанию** — `payments:smoke`, `payouts:smoke`, `payments:functional`, `payouts:functional`,
`mixed:functional`, `payments:baseline`, `payments:load`, `payments:spike`, `payments:stress`, `mixed:load`.

**Как идёт серия:**
- каждый шаг запускается на обеих версиях, каждая на свежем стеке своей версии;
- после шага `compare.mjs` сравнивает два прогона;
- нагрузочные шаги повторяются `LOAD_PAIRS` раз (2), и порядок версий в парах чередуется;
- регрессия в `smoke` или `functional` останавливает серию, если не задан `SERIES_CONTINUE=1`;
- регрессия нагрузки только отмечается, вывод делается по всем парам.

Переменные профилей действуют на все шаги серии, например `DURATION=15m ... payments:soak`.

Результаты серии лежат в `tests/k6/results/_series-<эталон>-<кандидат>-<время>/`:

| Файл | Содержимое |
| --- | --- |
| `series.log` | ход серии: старт и итог каждого прогона, сравнения |
| `summary.md` | все прогоны и сравнения одной таблицей |
| `<NN>-<набор>-<профиль>.compare.md` | сравнение пары |
| `<NN>-<набор>-<профиль>-<версия>.*` | журнал k6 и Compose, CPU и память контейнеров, сводка запросов ClickHouse |

Следить за ходом: `tail -f tests/k6/results/_series-*/series.log`. Итог серии оформляется отчётом в
[reports](reports/README.md).

## Результаты и сравнение

Одиночный прогон пишет в `tests/k6/results/<набор>-<профиль>-<версия>-<время>/`:
- `report.md` — итог;
- `summary.json` — метрики;
- `run.log` — вывод k6.

Сравнить два прогона:

```bash
node tests/k6/tools/compare.mjs <прогон эталона> <прогон кандидата>
```

**Регрессия** — это одно из двух:
- кейс прошёл на эталоне и не прошёл на кандидате;
- p95 этапа вырос больше чем на `TOLERANCE_PERCENT` (20%).

Кейс, пропущенный из-за функциональностей версии, регрессией не считается. p95 заметно меняется от прогона к прогону,
поэтому о задержке судят по нескольким парам прогонов.

## Kubernetes

Тот же набор можно запустить Job-ом внутри кластера, рядом с сервисами:

```bash
K8S_NAMESPACE=<namespace> VERSION=<подпись> scripts/run-k6.sh k8s payments functional
```

**Как это работает.** Скрипт упаковывает `tests/k6` в ConfigMap и запускает Job по [k8s/job.yaml](k8s/job.yaml).
Результаты он забирает в `tests/k6/results/`, а Job и ConfigMap удаляет.

**Образ с тестами.** CI собирает `tests/k6` в образ `ghcr.io/valitydev/fraudbusters-k6:sha-<коммит>` по
[Dockerfile](Dockerfile) и публикует его из master, когда меняется `tests/k6`. С `K6_TESTS_IMAGE` Job запускает
тесты из образа по [k8s/job-image.yaml](k8s/job-image.yaml), без упаковки в ConfigMap:

```bash
K6_TESTS_IMAGE=ghcr.io/valitydev/fraudbusters-k6:sha-<коммит> K8S_NAMESPACE=<namespace> VERSION=<подпись> \
    scripts/run-k6.sh k8s payments functional
```

Скрипт и инструменты отчёта берутся из checkout, поэтому его коммит должен совпадать с коммитом образа. Образ можно
собрать и проверить локально:

```bash
docker build -t ghcr.io/valitydev/fraudbusters-k6:local tests/k6
scripts/check-k6-image.sh ghcr.io/valitydev/fraudbusters-k6:local
```

**Запуск без скрипта.** Среда может запускать образ своим Job-ом, например из своего CI:
- команда `sh /k6/k8s/run-in-pod.sh`, пользователь 12345;
- переменные `SUITE`, `K6_PROFILE`, `VERSION`, `RESULTS_DIR=/results` и ConfigMap и Secret из таблицы ниже; профили
  и кейсы задаются теми же переменными, что и в `run-k6.sh`;
- под пишет в `/results` файлы `run.log`, `summary.json` и `exit-code`, в конце создаёт `done` и ждёт
  `HOLD_SECONDS` (3600), чтобы результаты успели забрать `kubectl cp`;
- `node tools/report.mjs <результаты>` строит `report.md`, `node tools/compare.mjs` сравнивает два прогона.

**Что нужно от среды.**
- Адреса и учётные данные Job берёт из своего namespace:

  | Объект | Ключи |
  | --- | --- |
  | ConfigMap `k6-regression` | `FB_MANAGEMENT_URL`, `FB_API_URL`, `FB_INSPECTOR_URL`; для токена — `FB_AUTH_URL`, `FB_AUTH_REALM`, `FB_AUTH_CLIENT`; при необходимости `FEATURES` |
  | Secret `k6-regression`, необязательный | `FB_AUTH_USER` и `FB_AUTH_PASSWORD` пользователя с ролью `fraud-officer`, или `FB_TOKEN` |

- Поду с меткой `app.kubernetes.io/name: k6-regression` нужен доступ к этим адресам.
- Пользователю `kubectl` нужны права на Job, ConfigMap, `pods/log` и `pods/exec`.

| Переменная | Значение |
| --- | --- |
| `K8S_NAMESPACE` | namespace; по умолчанию namespace текущего контекста |
| `VERSION` | подпись прогона (`k8s`) |
| `K6_TESTS_IMAGE` | образ с тестами; без переменной тесты упаковываются в ConfigMap из checkout |
| `K8S_CPU`, `K8S_MEMORY` | ресурсы пода k6 (`1`, `1Gi`) |
| `K8S_CONFIG`, `K8S_SECRET` | другие имена ConfigMap и Secret |
| `K8S_DRY_RUN=1` | напечатать манифест и ничего не создавать |
| `K8S_KEEP=1` | не удалять Job и ConfigMap |

`lifecycle` в кластере идёт в два этапа с одним `RUN_ID`:
- `K6_PHASE=prepare` — до смены версии;
- `K6_PHASE=verify` — после смены, с `FROM_FEATURES` версии, на которой шла подготовка.

## Как добавить кейс или набор

Кейс:

1. `data/<набор>/<ID>.js` — данные, поля в таблице ниже.
2. `suites/<набор>/cases/<ID>.js` с полями:
   - `id`, `title`, `profiles`;
   - при необходимости `since` или `until`;
   - `run(context)` на шагах `steps.js`.
3. Строка в `suites/<набор>/cases/index.js`.
4. Описание в `suites/<набор>/README.md`.

| Поле данных | Значение |
| --- | --- |
| `template` | текст шаблона Fraudo |
| `ready` | признак, что шаблон применился; по умолчанию `{ decision: 'low' }` |
| `loads` | история перед проверкой: статус или `{ status, with, errorCode }` |
| `expected` | ожидаемые решения REST (`low`, `high`, `fatal`) или сохранённые `<статус>/<правило>` |

Значение без `expected` записывается как наблюдение: кейс оно не роняет, но сравнивается между прогонами.

**Плейсхолдеры.** Значения, зависящие от прогона, задаются плейсхолдерами:
- `{{user}}` превращается в `k6-<run>-<case>-user`;
- `{{prefix}}` превращается в `k6-<run>-<case>`.

**Новый набор** — папка `suites/<набор>/` с файлами:
- `main.js` с `defineSuite`;
- `cases/`;
- `README.md`.

Набор также нужно добавить строкой в [suites/README.md](suites/README.md).
