# Регресс XYZ-598 (выплаты), 2026-11-16

Функциональный регресс платежей на версии до выплат и после них, и функциональные наборы выплат на новой версии.

## Данные прогона

| | |
| --- | --- |
| Дата | 2026-11-16 |
| Стенд | локальный, `scripts/run-k6.sh up`, Docker Desktop 10 ГБ, каждая версия на свежем стеке без данных |
| Тесты | `tests/k6` до рефакторинга структуры (те же кейсы матрицы, код разложен по файлам приоритетов) |
| Сравнение | `tools/compare.mjs`, допуск p95 20% |

| Версия | Коммит | Образы |
| --- | --- | --- |
| [before-payouts](../../versions/before-payouts.env) | `bc0480a8` | `sha-bc0480a`, wb-list-manager `sha-4743641` |
| [after-payouts](../../versions/after-payouts.env) | `18c71eab` | `sha-306afdb`, fraudbusters-management `sha-45f6deb` |

```bash
scripts/run-k6.sh up before-payouts
scripts/run-k6.sh test before-payouts payments functional
scripts/run-k6.sh test before-payouts payments baseline
scripts/run-k6.sh down
scripts/run-k6.sh up after-payouts
scripts/run-k6.sh test after-payouts payments functional
scripts/run-k6.sh test after-payouts payments baseline
scripts/run-k6.sh test after-payouts payouts functional
scripts/run-k6.sh test after-payouts mixed functional
scripts/run-k6.sh lifecycle before-payouts after-payouts
```

## Итог

| Набор | before-payouts | after-payouts |
| --- | --- | --- |
| `payments functional`, 50 кейсов | 49 passed, 1 skipped | 48 passed, **1 failed** (PAY-FL-16), 1 skipped |
| `payments baseline`, 10/с, 5 мин | 0 ошибок, p95 проверки 104 мс | 0 ошибок, p95 68 мс |
| `payouts functional`, 26 кейсов | — | 25 passed, **1 failed** (PO-API-11) |
| `mixed functional`, 4 кейса | — | 4 passed |
| `lifecycle`: перезапуск, обновление с данными | LC-01…05 passed | LC-01…05 после обновления, LC-01…04, LC-06 после перезапуска passed |

`payments functional` — один прогон 48 кейсов и отдельный прогон PAY-API-11 и PAY-API-17, добавленных позже.
Skipped — PAY-FL-21: глобальную привязку платежей нельзя создать через REST API.
Наблюдения кейсов (границы счётчиков, коды ответов, порядок приоритетов) на двух версиях совпали.

## Различия версий

**PAY-FL-16, `isTrusted(paymentsConditions(...))`.** Коммит `e01057aa` убрал из грамматики Fraudo формы
`isTrusted(paymentsConditions(...))` и `isTrusted(withdrawalsConditions(...))`; остались `isTrusted()` и
`isTrusted("template")`. На новой версии такой шаблон не проходит валидацию. При обновлении с данными (LC-05)
fraudbusters пишет WARN, применяет остальные правила шаблона, а правило с `isTrusted` молча выпадает.
Решение от 2026-11-17: в продакшен-шаблонах `isTrusted` не используется, несовместимость принята.

**PO-API-11.** `GET /payouts-templates/filter?size=-1` отвечает 500 (`LIMIT must not be negative`); остальные неверные
параметры API выплат отвечают 400.

## Дефекты, общие для обеих версий

| Кейс | Поведение |
| --- | --- |
| PAY-API-19 | `sortOrder=FOO`, неизвестный `listType`, `size=-1` в management — 500 вместо 400 |
| PAY-FL-11 | `/inspect-payment` с `userId` без `customerId` — 500 (`Required field 'customer_id'`) |
| PAY-API-13 | `POST /fraud-payments` отвечает 201, но запись не сохраняется (ISO-время не разбирается); тип и комментарий из CSV пустые |
| PAY-API-09 | `POST /payments-lists` с типом `naming` — 500 |
| PAY-API-14 | фильтр `invoiceId` в `inspect-results` не сужает выборку |
| PAY-API-10 | CSV серого списка всегда отклоняется (500) |
| PAY-API-15 | `applyRuleOnHistoricalDataSet` возвращает id результата, а `GET /checked-data-sets/{id}` ищет по id набора; фильтр наборов без `from`/`to` и `GET` удалённого набора — 500 |
| PAY-API-11 | кандидат по fingerprint создаётся в списке `FINGERPRRINT`, который `inBlackList("fingerprint")` не читает |
| PAY-API-17 | аналитика возвращает сумму в основных единицах; `fraud-payments/count` считает все платежи |
| PAY-FL-27 | `/inspect-payment` отвечает 500 на решение `trust`; код ошибки с пробелом в правиле не совпадает с кодом в истории; явный email `UNKNOWN` приводится к `unknown` |
| PAY-FL-28 | `catch: decline` из документации Fraudo грамматикой не допускается; `notificationsRule` пустой |
| PAY-FL-29 | `not A and B` разбирается как `not (A and B)` |
| PAY-FL-31 | `count("masked_pan", …)` проходит валидацию, но поля нет в истории; агрегат по `country_bank` не срабатывает |
| PAY-FL-18, PAY-FL-25 | через REST не передаются `isRecurrent`, `isMobile`, `paymentSystem()`, `cardCategory()`; модель `/inspect-user` без суммы, ip, fingerprint |
| PO-API-12 | `POST /withdrawals` принимает `walletId` без `partyId` |
| MIX-04 | пороги суммы больше 2³¹ не срабатывают |

## Не покрыто

- PAY-FL-15, PAY-FL-21: именованные списки и глобальная привязка платежей через REST не создаются.
- Путь machinegun → mg-connector → ClickHouse: локально выключен.
- Нагрузка: [2026-11-17-load](../2026-11-17-load/README.md).
