# k6: регрессионные и нагрузочные тесты Fraudbusters

Тесты проверяют сквозные сценарии через публичные API: fraudbusters-management, fraudbusters-api и Thrift-инспектор
выплат. Все кейсы перечислены в [TEST_CASES.md](TEST_CASES.md). Нужен только Docker; отчёты строит Node.js.

```bash
scripts/run-k6.sh up after-payouts
scripts/run-k6.sh test after-payouts payments functional
scripts/run-k6.sh down
```

## Где что лежит

Кейс из матрицы — два файла с его ID:

| Что | Где |
| --- | --- |
| Код кейса | `suites/<набор>/cases/<ID>.js`, например [PAY-FL-04](suites/payments/cases/PAY-FL-04.js) |
| Данные кейса | `data/<набор>/<ID>.js`, например [PAY-FL-04](data/payments/PAY-FL-04.js) |
| Данные нагрузки | [data/traffic.js](data/traffic.js) |
| Отчёты прогонов | [reports](reports/README.md), папка на прогон: `reports/<дата>-<вид>/README.md` |
| Версии локального стенда | [versions](versions): теги опубликованных образов каждого сервиса |

```
data/                шаблоны правил, операции, ожидаемые решения — по файлу на кейс
suites/<набор>/      main.js, cases/<ID>.js, steps.js — шаги этого набора
suites/traffic.js    нагрузочный трафик наборов payments и mixed
lib/rules.js         общие шаги платежей и выплат: шаблоны, привязки, решения, история, списки
lib/                 запуск наборов и кейсов, профили, проверки, ожидание, пагинация, Thrift
clients/ fixtures/   HTTP- и Thrift-клиенты, модели платежа и выплаты
tools/               готовность стенда, отчёт прогона, сравнение двух прогонов
k8s/                 Job и скрипт пода для запуска в кластере
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

Без `expected` значение записывается как наблюдение: оно не роняет кейс, но сравнивается между версиями.

## Наборы и профили

```bash
scripts/run-k6.sh test <версия> <набор> <профиль>
```

| Набор | Что проверяет | Версии |
| --- | --- | --- |
| `payments` | проверка платежа, история, каскад шаблонов, списки, API management | обе |
| `payouts` | `InspectWithdrawal`, история, каскад, списки, API выплат | `after-payouts` |
| `mixed` | влияние выплат на платежи и наоборот, разделение конфигураций | `after-payouts` |
| `lifecycle` | конфигурация и история после перезапуска или обновления версии | обе |

| Профиль | Что запускает |
| --- | --- |
| `functional` | все кейсы набора параллельно (`CONCURRENCY`, 6) |
| `smoke` | кейсы с пометкой smoke |
| `baseline` | `RATE` итераций/с (10) в течение `DURATION` (5m) |
| `load` | разгон до `TARGET_RATE` (2×`RATE`), плато, смена шаблона на `RECONFIGURE_AT` (3m) |
| `stress` | ступени `RATE` × `STRESS_FACTORS` (1,2,3,4) |
| `spike` | всплеск до `SPIKE_RATE` (4×`RATE`) |
| `soak` | `RATE` в течение `DURATION` (1h) |

Отдельные кейсы: `K6_CASES=PAY-FL-19,PAY-FL-20 scripts/run-k6.sh test after-payouts payments functional`.
Доли трафика — `SHARE_<поток>` из [data/traffic.js](data/traffic.js). Перезапуск и обновление:

```bash
scripts/run-k6.sh lifecycle after-payouts
scripts/run-k6.sh lifecycle before-payouts after-payouts
```

Стек `fb-regression` — отдельный Compose-проект без проброса портов, поэтому рядом может работать обычный
локальный стек. Локально он выдерживает около 40 итераций/с; каждую серию нагрузки начинайте со свежего стека.

## Запуск в Kubernetes

Те же наборы и профили запускаются Job-ом внутри кластера, рядом с сервисами: нагрузка не идёт через ingress,
а Thrift-инспектор доступен напрямую. Нужен `kubectl` с контекстом кластера.

```bash
K8S_NAMESPACE=<namespace> VERSION=before-payouts scripts/run-k6.sh k8s payments functional
K8S_NAMESPACE=<namespace> VERSION=before-payouts RATE=20 K8S_CPU=2 K8S_MEMORY=2Gi scripts/run-k6.sh k8s payments load
```

Скрипт упаковывает `tests/k6` в ConfigMap, создаёт Job по [k8s/job.yaml](k8s/job.yaml), показывает вывод k6,
забирает результаты в `tests/k6/results/<набор>-<профиль>-<VERSION>-<время>/`, строит `report.md` и удаляет Job и
ConfigMap. Перед прогоном под ждёт готовности среды (`READY_TIMEOUT`, 600 с). `VERSION` — подпись прогона:
прогоны двух версий сравнивает `tools/compare.mjs`, как и локальные.

Тесты не знают устройства среды: адреса и учётные данные Job берёт из ConfigMap и Secret своего namespace,
их создаёт конфигурация среды.

| Объект | Ключи |
| --- | --- |
| ConfigMap `K8S_CONFIG` (`k6-regression`) | `FB_MANAGEMENT_URL`, `FB_API_URL`, `FB_INSPECTOR_URL`; для токена — `FB_AUTH_URL`, `FB_AUTH_REALM`, `FB_AUTH_CLIENT` |
| Secret `K8S_SECRET` (`k6-regression`), необязательный | `FB_AUTH_USER` и `FB_AUTH_PASSWORD` пользователя с ролью `fraud-officer`, или готовый `FB_TOKEN` |

С `FB_AUTH_URL` k6 сам получает токен у Keycloak по логину и паролю и обновляет его в долгих прогонах. Поду с
меткой `app.kubernetes.io/name: k6-regression` нужен доступ к этим адресам, а пользователю `kubectl` — права на
Job, ConfigMap, `pods/log` и `pods/exec` в namespace.

| Переменная | Значение |
| --- | --- |
| `K8S_NAMESPACE` | namespace среды; иначе namespace текущего контекста |
| `VERSION` | подпись прогона, по умолчанию `k8s` |
| `K8S_CPU`, `K8S_MEMORY` | ресурсы пода k6, по умолчанию `1` и `1Gi` |
| `K8S_CONFIG`, `K8S_SECRET` | имена ConfigMap и Secret с настройками среды |
| `K6_IMAGE` | образ k6, по умолчанию `grafana/k6:0.54.0` |
| `K8S_DRY_RUN=1` | напечатать манифест Job и ничего не создавать |
| `K8S_KEEP=1` | не удалять Job и ConfigMap после прогона |

`lifecycle` в кластере запускается по этапам с одним `RUN_ID`: `K6_PHASE=prepare` до смены версии и
`K6_PHASE=verify` после неё, с `FROM_VERSION` — версией, на которой шла подготовка.

## Результаты и сравнение версий

Прогон пишет в `tests/k6/results/<набор>-<профиль>-<версия>-<время>/` файлы `run.log` (строка `case_finished` на
кейс), `summary.json` и `report.md`. Сравнение двух прогонов:

```bash
node tests/k6/tools/compare.mjs tests/k6/results/<прогон старой версии> tests/k6/results/<прогон новой версии>
```

Сравнение падает, если кейс прошёл на старой версии и не прошёл на новой, или p95 этапа вырос больше чем на 20%.
p95 на локальном стенде меняется между прогонами в 2–3 раза, поэтому о задержке судят только по нескольким парам
прогонов с чередованием порядка версий.

## Как добавить кейс

1. Карточка в [TEST_CASES.md](TEST_CASES.md) с новым ID.
2. `data/<набор>/<ID>.js` с шаблоном, операциями и ожиданиями.
3. `suites/<набор>/cases/<ID>.js` с `id`, `title`, `profiles` и `run(context)` на шагах `steps.js`.
4. Строка в `suites/<набор>/cases/index.js`.
