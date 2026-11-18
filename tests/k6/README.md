# k6: функциональные и нагрузочные тесты Fraudbusters

Тесты проверяют сквозные сценарии через публичные API: fraudbusters-management, fraudbusters-api и Thrift-инспектор.
Наборы, их кейсы и общие соглашения описаны в [suites](suites/README.md). Нужен только Docker; отчёты строит Node.js.

```bash
scripts/run-k6.sh up 1119c3f1
scripts/run-k6.sh test 1119c3f1 payments functional
scripts/run-k6.sh down
```

## Где что лежит

```
suites/README.md       наборы, функциональности, общие соглашения, профили, нагрузка
suites/<набор>/        README.md с кейсами, main.js, cases/<ID>.js, steps.js
data/<набор>/<ID>.js   данные кейса: шаблоны правил, операции, ожидаемые решения
data/traffic.js        данные нагрузочного трафика
suites/traffic.js      нагрузочный трафик наборов payments и mixed
lib/                   запуск наборов и кейсов, функциональности, профили, проверки, ожидание, пагинация, Thrift
lib/rules.js           общие шаги платежей и выплат: шаблоны, привязки, решения, история, списки
clients/ fixtures/     HTTP- и Thrift-клиенты, модели платежа и выплаты
versions/              версии локального стенда: теги образов и функциональности
tools/                 готовность стенда, отчёт прогона, сравнение двух прогонов
k8s/                   Job и скрипт пода для запуска в кластере
reports/               отчёты прогонов, папка на прогон: <дата>-<вид>/README.md
```

## Данные кейса

Файл данных — обычный JS-объект: шаблон правил текстом, операции, ожидаемые решения. Значения, зависящие от прогона,
записаны плейсхолдерами: `{{user}}` превращается в `k6-<run>-<case>-user`, `{{prefix}}` — в `k6-<run>-<case>`.

| Поле | Значение |
| --- | --- |
| `template` | текст шаблона Fraudo, как он сохраняется в management |
| `ready` | как понять, что шаблон применился: поля операции и решение, по умолчанию `{ decision: 'low' }` |
| `loads` | история перед проверкой: статус строкой или `{ status, with, errorCode }`, `with` — поля операции |
| `expected` | ожидаемые решения REST (`low`, `high`, `fatal`) или сохранённые `<статус>/<правило>` |

Без `expected` значение записывается как наблюдение: оно не роняет кейс, но сравнивается между прогонами.

## Запуск на локальном стенде

```bash
scripts/run-k6.sh up <версия>
scripts/run-k6.sh test <версия> <набор> <профиль>
scripts/run-k6.sh lifecycle <версия> [<версия после обновления>]
scripts/run-k6.sh down
```

Версия — файл [versions](versions)`/<версия>.env`: теги опубликованных образов каждого сервиса и `FEATURES`,
функциональности этой версии. Кейсы, которым нужна отсутствующая функциональность, получают статус `skipped`.
`INFRA_COMMIT` — коммит, из которого берётся `infra/` стенда (ClickHouse, Kafka, базы): версия запускается на своей
инфраструктуре, а не на текущей. Скрипт выгружает её в `tests/k6/.infra/<коммит>`. Без `INFRA_COMMIT` берётся
`infra/` текущего дерева. `lifecycle` требует одной инфраструктуры у обеих версий.

Образы версии, ещё не опубликованные CI (CI публикует только master), собираются локально и получают тег версии:

```bash
scripts/build-local-images.sh fraudbusters fraudbusters-warehouse
docker tag ghcr.io/valitydev/fraudbusters:local ghcr.io/valitydev/fraudbusters:sha-<коммит>
```

| Профиль | Что запускает |
| --- | --- |
| `functional` | все кейсы набора параллельно (`CONCURRENCY`, 6) |
| `smoke` | кейсы с пометкой smoke |
| `baseline` | `RATE` итераций/с (10) в течение `DURATION` (5m) |
| `load` | разгон до `TARGET_RATE` (2×`RATE`), плато, смена шаблона на `RECONFIGURE_AT` (3m) |
| `stress` | ступени `RATE` × `STRESS_FACTORS` (1,2,3,4) |
| `spike` | всплеск до `SPIKE_RATE` (4×`RATE`) |
| `soak` | `RATE` в течение `DURATION` (1h) |

Отдельные кейсы: `K6_CASES=PAY-FL-19,PAY-FL-20 scripts/run-k6.sh test <версия> payments functional`.
Доли трафика — `SHARE_<поток>` из [data/traffic.js](data/traffic.js).

Стек `fb-regression` — отдельный Compose-проект без проброса портов, поэтому рядом может работать обычный
локальный стек. Локально он выдерживает около 40 итераций/с; каждую серию нагрузки начинайте со свежего стека.

## Запуск в Kubernetes

Те же наборы и профили запускаются Job-ом внутри кластера, рядом с сервисами: нагрузка не идёт через ingress,
а Thrift-инспектор доступен напрямую. Нужен `kubectl` с контекстом кластера.

```bash
K8S_NAMESPACE=<namespace> VERSION=<подпись> scripts/run-k6.sh k8s payments functional
K8S_NAMESPACE=<namespace> VERSION=<подпись> RATE=20 K8S_CPU=2 K8S_MEMORY=2Gi scripts/run-k6.sh k8s payments load
```

Скрипт упаковывает `tests/k6` в ConfigMap, создаёт Job по [k8s/job.yaml](k8s/job.yaml), показывает вывод k6,
забирает результаты в `tests/k6/results/<набор>-<профиль>-<VERSION>-<время>/`, строит `report.md` и удаляет Job и
ConfigMap. Перед прогоном под ждёт готовности среды (`READY_TIMEOUT`, 600 с).

Тесты не знают устройства среды: адреса и учётные данные Job берёт из ConfigMap и Secret своего namespace,
их создаёт конфигурация среды.

| Объект | Ключи |
| --- | --- |
| ConfigMap `K8S_CONFIG` (`k6-regression`) | `FB_MANAGEMENT_URL`, `FB_API_URL`, `FB_INSPECTOR_URL`; для токена — `FB_AUTH_URL`, `FB_AUTH_REALM`, `FB_AUTH_CLIENT`; при необходимости `FEATURES` |
| Secret `K8S_SECRET` (`k6-regression`), необязательный | `FB_AUTH_USER` и `FB_AUTH_PASSWORD` пользователя с ролью `fraud-officer`, или готовый `FB_TOKEN` |

С `FB_AUTH_URL` k6 сам получает токен у Keycloak по логину и паролю и обновляет его в долгих прогонах. Поду с
меткой `app.kubernetes.io/name: k6-regression` нужен доступ к этим адресам, а пользователю `kubectl` — права на
Job, ConfigMap, `pods/log` и `pods/exec` в namespace.

| Переменная | Значение |
| --- | --- |
| `K8S_NAMESPACE` | namespace среды; иначе namespace текущего контекста |
| `VERSION` | подпись прогона, по умолчанию `k8s` |
| `FEATURES` | функциональности развёрнутой версии; без переменной — все, как в текущем master; `FEATURES=` — ни одной |
| `K8S_CPU`, `K8S_MEMORY` | ресурсы пода k6, по умолчанию `1` и `1Gi` |
| `K8S_CONFIG`, `K8S_SECRET` | имена ConfigMap и Secret с настройками среды |
| `K6_IMAGE` | образ k6, по умолчанию `grafana/k6:0.54.0` |
| `K8S_DRY_RUN=1` | напечатать манифест Job и ничего не создавать |
| `K8S_KEEP=1` | не удалять Job и ConfigMap после прогона |

`lifecycle` в кластере запускается по этапам с одним `RUN_ID`: `K6_PHASE=prepare` до смены версии и
`K6_PHASE=verify` после неё, с `FROM_FEATURES` — функциональностями версии, на которой шла подготовка.

## Серия: сравнение двух версий от smoke до нагрузки

```bash
scripts/run-k6.sh series <эталон> <кандидат>
scripts/run-k6.sh series <эталон> <кандидат> payments:functional payments:load
```

Шаг — `<набор>:<профиль>`. По умолчанию шаги идут от быстрых к тяжёлым: `payments:smoke`, `payouts:smoke`,
`payments:functional`, `payouts:functional`, `mixed:functional`, `payments:baseline`, `payments:load`,
`payments:spike`, `payments:stress`, `mixed:load`; на локальном стенде это около 5 часов. `soak` в серию по умолчанию не входит:
`DURATION=15m scripts/run-k6.sh series <эталон> <кандидат> payments:soak`.

Каждый прогон идёт на свежем стеке своей версии: `down`, `up`, `test`, снимок ClickHouse, `down`. Шаг выполняется
на обеих версиях и сравнивается `compare.mjs`. Нагрузочные шаги повторяются `LOAD_PAIRS` раз (2), порядок версий
меняется в каждой паре, поэтому ни одна версия не идёт всегда первой. Регресс в `smoke` или `functional`
останавливает серию (`SERIES_CONTINUE=1` — продолжить); регресс нагрузки только отмечается, решение — по всем парам.
Остальные переменные (`RATE`, `DURATION`, …) действуют на все прогоны серии.

Серия пишет в `tests/k6/results/_series-<эталон>-<кандидат>-<время>/`:

| Файл | Содержимое |
| --- | --- |
| `series.log` | строка на событие: START, DONE с кодом выхода и папкой результатов, COMPARE, SERIES COMPLETE или STOPPED |
| `summary.md` | таблица прогонов со ссылками на `report.md` и таблица сравнений, пишется в конце |
| `<NN>-<набор>-<профиль>-<версия>.k6.log` | вывод k6 прогона; `.compose.log` — подъём и остановка стека |
| `<NN>-<набор>-<профиль>-<версия>.stats.csv` | CPU и память контейнеров стека каждые `STATS_INTERVAL` секунд (15) |
| `<NN>-<набор>-<профиль>-<версия>.clickhouse.txt` | версия ClickHouse, число запросов, p95 SELECT, пик памяти запроса |
| `<NN>-<набор>-<профиль>.compare.md` | вывод `compare.mjs` по паре |

Ход серии: `tail -f tests/k6/results/_series-*/series.log`. Итог серии оформляется отчётом в [reports](reports).

## Результаты и сравнение прогонов

Прогон пишет в `tests/k6/results/<набор>-<профиль>-<версия>-<время>/` файлы `run.log` (строка `case_finished` на
кейс), `summary.json` и `report.md`. Сравнение двух прогонов, например одной среды до и после обновления:

```bash
node tests/k6/tools/compare.mjs tests/k6/results/<первый прогон> tests/k6/results/<второй прогон>
```

Сравнение падает, если кейс прошёл в первом прогоне и упал во втором, или p95 этапа вырос больше чем на
`TOLERANCE_PERCENT` (20%). Кейс, пропущенный во втором прогоне из-за функциональностей версии, регрессией не считается.
p95 на локальном стенде меняется между прогонами в 2–3 раза, поэтому о задержке судят по нескольким парам прогонов
с чередованием порядка.

## Как добавить кейс или набор

Кейс:

1. `data/<набор>/<ID>.js` с шаблоном, операциями и ожиданиями.
2. `suites/<набор>/cases/<ID>.js` с `id`, `title`, `profiles`, при необходимости `since` или `until`, и `run(context)`
   на шагах `steps.js`.
3. Строка в `suites/<набор>/cases/index.js`.
4. Карточка в `suites/<набор>/README.md`.

Набор — новая папка `suites/<набор>/` с `main.js` (`defineSuite`, `since`, если набору нужна функциональность),
`cases/`, `README.md` и строкой в [suites/README.md](suites/README.md). Новая функциональность описана там же.
