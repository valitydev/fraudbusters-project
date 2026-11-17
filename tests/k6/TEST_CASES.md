# Матрица тест-кейсов k6

Документ перечисляет все проверки регрессионного и нагрузочного набора `tests/k6`.
Каждый кейс имеет постоянный ID: он совпадает с тегом `case_id` в метриках и строкой в отчёте прогона.
Код кейса — `suites/<набор>/cases/<ID>.js`, его данные (шаблоны, операции, ожидания) — `data/<набор>/<ID>.js`.
Новый кейс сначала добавляется сюда, затем в код. Отчёты прогонов — в [reports](reports/README.md).

## Содержание

1. [Назначение и порядок регресса](#назначение-и-порядок-регресса)
2. [Наборы и версии](#наборы-и-версии)
3. [Общие соглашения](#общие-соглашения)
4. [Сводная матрица](#сводная-матрица)
5. [Платежи: сквозные сценарии (PAY-FL)](#платежи-сквозные-сценарии-pay-fl)
6. [Платежи: API управления и загрузки (PAY-API)](#платежи-api-управления-и-загрузки-pay-api)
7. [Выплаты: сквозные сценарии (PO-FL)](#выплаты-сквозные-сценарии-po-fl)
8. [Выплаты: API управления и загрузки (PO-API)](#выплаты-api-управления-и-загрузки-po-api)
9. [Жизненный цикл: перезапуск и обновление (LC)](#жизненный-цикл-перезапуск-и-обновление-lc)
10. [Платежи и выплаты вместе (MIX)](#платежи-и-выплаты-вместе-mix)
11. [Нагрузка (NF)](#нагрузка-nf)
12. [Инварианты пагинации (PG)](#инварианты-пагинации-pg)
13. [Пробелы покрытия](#пробелы-покрытия)
14. [Профили запуска](#профили-запуска)
15. [Вне рамок](#вне-рамок)

## Назначение и порядок регресса

Набор проверяет доработку выплат (XYZ-598) и то, что она не изменила поведение платежей.

| Шаг | Версия стека | Что запускаем | Критерий |
| --- | --- | --- | --- |
| 1 | Старая: `bc0480a8`, `versions/before-payouts.env` | `payments functional` | все кейсы PAY-* зелёные |
| 2 | Старая | `payments baseline` | сохранён эталон производительности |
| 3 | Новая: `18c71eab`, `versions/after-payouts.env` | `payments functional` | результат каждого кейса совпадает с шагом 1 |
| 4 | Новая | `payments baseline` | p95 этапов в пределах допуска относительно шага 2 |
| 5 | Новая | `payouts functional`, `mixed functional` | все кейсы PO-* и MIX-* зелёные |
| 6 | Новая | нагрузочные профили NF-* | пороги выполнены |

`bc0480a8` — последний коммит master перед `e01057aa Add proto and rules for payouts (#5)`.
Версии запускаются из опубликованных CI-образов (см. [versions](versions)) на одной инфраструктуре
(`infra/compose.yaml` текущего master):
изменения инфраструктуры в XYZ-598 только добавляют топики и колонки выплат.

Если кейс PAY-* падает на старой версии, сначала выясняется причина: ошибка теста
исправляется, а известный дефект старой версии фиксируется в кейсе с пометкой
«известный дефект». Сравнение версий начинается только после зелёного шага 1.
Граничные значения агрегатов (входит ли текущая операция в счётчик, включительность
окна) берутся из прогона на старой версии и становятся эталоном для новой.

## Наборы и версии

| Набор | Кейсы | Старая версия | Новая версия |
| --- | --- | --- | --- |
| `payments` | PAY-FL-*, PAY-API-*, NF-01…NF-08 | да | да |
| `payouts` | PO-FL-*, PO-API-* | нет | да |
| `mixed` | MIX-*, NF-09 | нет | да |

Набор `payments` использует только API и функции Fraudo, которые есть в обеих версиях.
Функции истории выплат в правилах платежей проверяются только в `mixed`.

## Общие соглашения

**Точки входа (локальный стек).**

| Сервис | Адрес из контейнера k6 | Адрес с хоста |
| --- | --- | --- |
| Management API | `http://fb-management:8080/fb-management/v1` | `http://localhost:8085/fb-management/v1` |
| Inspection API (fraudbusters-api) | `http://fraudbusters-api:8080` | `http://localhost:9999` |
| Inspector Thrift (выплаты) | `http://fraudbusters:8022/fraud_inspector/v1` | `http://localhost:8999/fraud_inspector/v1` |

Локально management работает с `keycloak.enabled=false`, токен не нужен.

**Изоляция данных.** У каждого прогона есть `run` (короткий случайный идентификатор).
Все party, shop, wallet, шаблоны, группы, email, card_token, fingerprint, user_id
строятся как `k6-<run>-<кейс>-<роль>`. Поэтому кейсы не влияют друг на друга
и прогоны можно повторять без очистки. В идентификаторах нет `_` и `%`:
`searchValue` превращается в SQL `LIKE '%value%'`.

**Глобальный уровень.** Глобальная привязка шаблона одна на систему. Кейсы с меткой
«глобальный» выполняются последовательно в отдельном сценарии, правила в них
ограничены условием `in("party_id", "<party кейса>")`, а исходная глобальная
привязка восстанавливается после кейса.

**Распространение конфигурации.** Изменения в management доходят до fraudbusters через
Kafka асинхронно. После изменения кейс повторяет проверку с интервалом
`POLL_INTERVAL_MS` до ожидаемого результата или до `WAIT_TIMEOUT_MS`
(по умолчанию 3 с и 60 с). Так же ожидаются данные истории в ClickHouse.

**Результаты проверки.**

| Решение правила | Платёж, REST `result` | Выплата, Thrift `RiskScore` |
| --- | --- | --- |
| `accept`, `acceptAndNotify`, `notify` | `low` | `low` |
| `decline`, `declineAndNotify` | `fatal` | `fatal` |
| `trust` | — | `trusted` |
| `highRisk` | `high` | `high` |
| ни одно правило не сработало | `high` | `high` |

**Каскад шаблонов.** Применяется первое терминальное решение; результат `notify`
каскад не останавливает.

- Платёж: глобальный шаблон → группа party → группа party+shop → шаблон party → шаблон party+shop.
- Выплата: глобальный шаблон → глобальная группа → группа party → группа party+wallet →
  шаблон party → шаблон party+wallet. Для проверки выплаты обязательны `party_id` и `wallet_id`.

**Структура кейса.** Предусловия создаются в начале кейса через API и удаляются в конце.
«HTTP 2xx» сам по себе не считается успехом: каждый шаг проверяет тело ответа.

## Сводная матрица

Приоритет: P1 — входит в первую поставку, P2 — следующая итерация, P3 — по необходимости.
Профили: F — `functional`, S — `smoke`, L — нагрузочные профили (в составе трафика).

| ID | Название | Набор | Профили | Версии | P |
| --- | --- | --- | --- | --- | --- |
| PAY-FL-01 | Платёж без риска: проверка, история, результат проверки | payments | F S L | обе | P1 |
| PAY-FL-02 | Отклонение по сумме | payments | F S L | обе | P1 |
| PAY-FL-03 | Нет привязки или ни одно правило не сработало | payments | F S | обе | P1 |
| PAY-FL-04 | `count` по card_token в рамках party и shop | payments | F L | обе | P1 |
| PAY-FL-05 | `sum` по fingerprint с условием на валюту | payments | F | обе | P1 |
| PAY-FL-06 | `unique` email по ip | payments | F | обе | P1 |
| PAY-FL-07 | `countSuccess`, `countError`, `countPending` | payments | F | обе | P1 |
| PAY-FL-08 | `sumSuccess`, `sumError` | payments | F | обе | P2 |
| PAY-FL-09 | `countRefund`, `sumRefund` | payments | F | обе | P1 |
| PAY-FL-10 | `countChargeback`, `sumChargeback` | payments | F | обе | P1 |
| PAY-FL-11 | Идентичность клиента: `count("user_id", …, "party_id")` | payments | F L | обе | P1 |
| PAY-FL-12 | Чёрный список | payments | F S L | обе | P1 |
| PAY-FL-13 | Белый список | payments | F | обе | P1 |
| PAY-FL-14 | Серый список с лимитом | payments | F | обе | P1 |
| PAY-FL-15 | Именованный список `inList` (не выполняется) | payments | — | — | — |
| PAY-FL-16 | Доверенная карта `isTrusted` | payments | F | обе | P1 |
| PAY-FL-17 | Гео: `countryBy("ip")` | payments | F | обе | P2 |
| PAY-FL-18 | `isRecurrent`, `isMobile` | payments | F | обе | P3 |
| PAY-FL-19 | Каскад: шаблон party и party+shop | payments | F | обе | P1 |
| PAY-FL-20 | Каскад: группа и шаблон, приоритеты в группе | payments | F | обе | P1 |
| PAY-FL-21 | Каскад: глобальный шаблон (не выполняется) | payments | — | — | — |
| PAY-FL-22 | Каскад: `notify` не останавливает проверку | payments | F | обе | P1 |
| PAY-FL-23 | Привязка по умолчанию для нового магазина | payments | F | обе | P2 |
| PAY-FL-24 | Изменение и удаление шаблона, удаление привязки | payments | F | обе | P1 |
| PAY-FL-25 | Проверка пользователя `/inspect-user` | payments | F | обе | P2 |
| PAY-FL-26 | Загрузка fraud-платежей | payments | F | обе | P2 |
| PAY-FL-27 | Шаблон в форме боевого: списки, like, доверие, pending, коды ошибок | payments | F | обе | P1 |
| PAY-FL-28 | Результаты правил: 3ds, *AndNotify, уведомления, catch | payments | F | обе | P1 |
| PAY-FL-29 | Функции и операторы: userId, paymentSystem, cardCategory, rand, like, not, or, !=, дроби | payments | F | обе | P2 |
| PAY-FL-30 | Окна времени: minutes, calendar_days, calendar_months, смещение, без единицы | payments | F | обе | P2 |
| PAY-FL-31 | Агрегаты по полям: email, ip, fingerprint, phone, bin, masked_pan, country_bank | payments | F | обе | P2 |
| PAY-FL-32 | Thrift IsBlacklisted | payments | F | обе | P2 |
| PAY-API-01 | Валидация шаблона | payments | F | обе | P1 |
| PAY-API-02 | Создание, обновление, удаление шаблона | payments | F | обе | P1 |
| PAY-API-03 | Поиск шаблонов и пагинация | payments | F | обе | P1 |
| PAY-API-04 | Имена шаблонов | payments | F | обе | P1 |
| PAY-API-05 | Привязки шаблонов | payments | F | обе | P1 |
| PAY-API-06 | Привязки по умолчанию | payments | F | обе | P2 |
| PAY-API-07 | Группы шаблонов | payments | F | обе | P1 |
| PAY-API-08 | Привязки групп | payments | F | обе | P1 |
| PAY-API-09 | Списки: строки, имена, поиск | payments | F | обе | P1 |
| PAY-API-10 | Списки: загрузка и удаление CSV | payments | F | обе | P2 |
| PAY-API-11 | Списки: кандидаты | payments | F | обе | P3 |
| PAY-API-12 | История платежей: фильтры и пагинация | payments | F | обе | P1 |
| PAY-API-13 | История возвратов, чарджбэков, fraud-платежей | payments | F | обе | P1 |
| PAY-API-14 | История результатов проверки | payments | F | обе | P1 |
| PAY-API-15 | Наборы данных и применение правила к истории | payments | F | обе | P2 |
| PAY-API-16 | Эмуляция каскада шаблонов | payments | F | обе | P2 |
| PAY-API-17 | Аналитика | payments | F | обе | P3 |
| PAY-API-18 | Аудит | payments | F | обе | P2 |
| PAY-API-19 | Некорректные параметры management | payments | F | обе | P1 |
| PAY-API-20 | Загрузка данных в fraudbusters-api | payments | F | обе | P1 |
| PO-FL-01 | Выплата без риска: проверка, история | payouts | F S L | новая | P1 |
| PO-FL-02 | Нет привязки и неполный контекст | payouts | F S | новая | P1 |
| PO-FL-03 | Каскад шаблонов и групп выплат | payouts | F | новая | P1 |
| PO-FL-04 | Глобальная привязка выплат (глобальный) | payouts | F | новая | P1 |
| PO-FL-05 | Списки выплат и их область действия | payouts | F S L | новая | P1 |
| PO-FL-06 | `countPayouts`, `sumPayouts` | payouts | F L | новая | P1 |
| PO-FL-07 | `countPayoutsSuccess`, `sumPayoutsSuccess` | payouts | F | новая | P1 |
| PO-FL-08 | `countPayoutsError`, `sumPayoutsError` с кодом ошибки | payouts | F | новая | P1 |
| PO-FL-09 | `uniquePayouts` | payouts | F | новая | P2 |
| PO-FL-10 | Запрещённые конструкции в шаблоне выплаты | payouts | F | новая | P1 |
| PO-FL-11 | Изменение и удаление шаблона выплаты | payouts | F | новая | P1 |
| PO-FL-12 | Функции истории платежей в правиле выплаты | payouts | F | новая | P1 |
| PO-FL-13 | Группировка истории выплат по `wallet_id` | payouts | F | новая | P1 |
| PO-FL-14 | Списки выплат по полям, именованный список, countryBy | payouts | F | новая | P2 |
| PO-API-01 | Валидация шаблона выплаты | payouts | F | новая | P1 |
| PO-API-02 | Создание, обновление, удаление шаблона выплаты | payouts | F | новая | P1 |
| PO-API-03 | Поиск шаблонов выплат и пагинация | payouts | F | новая | P1 |
| PO-API-04 | Имена шаблонов выплат | payouts | F | новая | P1 |
| PO-API-05 | Привязки шаблонов выплат | payouts | F | новая | P1 |
| PO-API-06 | Группы шаблонов выплат | payouts | F | новая | P1 |
| PO-API-07 | Привязки групп выплат | payouts | F | новая | P1 |
| PO-API-08 | Списки выплат | payouts | F | новая | P1 |
| PO-API-09 | История выплат: фильтры, последнее состояние, пагинация | payouts | F | новая | P1 |
| PO-API-10 | Аудит команд выплат | payouts | F | новая | P2 |
| PO-API-11 | Некорректные параметры API выплат | payouts | F | новая | P1 |
| PO-API-12 | Загрузка выплат в fraudbusters-api | payouts | F | новая | P1 |
| MIX-01 | Успешные выплаты влияют на решение по платежу | mixed | F L | новая | P1 |
| MIX-02 | Платежи влияют на решение по выплате | mixed | F | новая | P1 |
| MIX-03 | Валидация функций выплат в шаблоне платежа | mixed | F | новая | P1 |
| MIX-04 | Конфигурации платежей и выплат не пересекаются | mixed | F | новая | P1 |
| LC-01…LC-04 | Конфигурация и история платежей после перезапуска и обновления | lifecycle | — | обе | P1 |
| LC-05 | Шаблон со снятым синтаксисом `isTrusted(paymentsConditions(...))` при обновлении | lifecycle | — | обновление | P1 |
| LC-06 | Конфигурация выплат после перезапуска | lifecycle | — | новая | P1 |
| NF-01 | Smoke | все | S | обе | P1 |
| NF-02 | Baseline платежей | payments | baseline | обе | P1 |
| NF-03 | Целевая нагрузка платежей | payments | load | обе | P1 |
| NF-04 | Stress платежей | payments | stress | обе | P3 |
| NF-05 | Spike платежей | payments | spike | обе | P3 |
| NF-06 | Soak платежей | payments | soak | новая | P3 |
| NF-07 | Изменение шаблона под нагрузкой | payments, mixed | load | обе | P3 |
| NF-08 | Сравнение baseline старой и новой версии | payments | baseline | обе | P1 |
| NF-09 | Смешанная нагрузка платежей и выплат | mixed | load, soak | новая | P1 |

---

## Платежи: сквозные сценарии (PAY-FL)

Во всех кейсах проверка платежа — `POST /inspect-payment`, загрузка истории —
`POST /payments` (`/refunds`, `/chargebacks`) в fraudbusters-api.
Шаблон привязывается к party+shop кейса, если не сказано иное.

### PAY-FL-01. Платёж без риска: проверка, история, результат проверки
- **Предусловия:** шаблон `rule: amount() >= 100000 -> decline; rule: amount() < 100000 -> accept;`, привязка к party+shop.
- **Шаги:**
  1. Проверить платёж на 5000 RUB.
  2. Загрузить этот платёж в историю со статусом `captured`.
  3. Найти платёж в `GET /payments-historical-data/payments-info` по `paymentId` в окне `from`/`to`.
  4. Найти проверку в `GET /payments-historical-data/inspect-results` по `paymentId`.
- **Ожидание:** шаг 1 — `low`; шаг 3 — одна запись со статусом `captured`, суммой и валютой платежа;
  шаг 4 — запись с результатом `accept` и ID шаблона кейса.

### PAY-FL-02. Отклонение по сумме
- **Предусловия:** шаблон из PAY-FL-01.
- **Шаги:** проверить платёж на 150000 RUB; найти проверку в истории результатов.
- **Ожидание:** `fatal`; в результате проверки решение `decline`, правило и шаблон кейса.

### PAY-FL-03. Нет привязки или ни одно правило не сработало
- **Шаги:**
  1. Проверить платёж party+shop без привязок.
  2. Привязать шаблон `rule: amount() > 1000000000 -> decline;` и проверить платёж на 5000 RUB.
- **Ожидание:** оба шага — `high`.

### PAY-FL-04. `count` по card_token в рамках party и shop
- **Предусловия:** шаблон `rule: count("card_token", 30, days, "party_id", "shop_id") > 2 -> decline; rule: amount() > 0 -> accept;`.
- **Шаги:**
  1. Проверить платёж с card_token кейса.
  2. Загрузить в историю `captured` платежи с тем же card_token по одному, после каждого проверяя новый платёж.
  3. Загрузить столько же платежей с этим card_token в другой shop.
- **Ожидание:** до порога — `low`; после порога — `fatal`; платежи другого shop не влияют на счётчик первого.
  Граничное количество фиксируется по старой версии.

### PAY-FL-05. `sum` по fingerprint с условием на валюту
- **Предусловия:** шаблон `rule: sum("fingerprint", 30, days, "party_id", "shop_id") > 20000 AND currency() = "RUB" -> decline; rule: amount() > 0 -> accept;`.
- **Шаги:** загрузить в историю платежи с fingerprint кейса на сумму ниже порога, проверить; догрузить до превышения, проверить; проверить платёж в USD.
- **Ожидание:** `low` → `fatal`; платёж в USD — `low`.

### PAY-FL-06. `unique` email по ip
- **Предусловия:** шаблон `rule: unique("ip", "email", 30, days, "party_id", "shop_id") > 1 -> decline; rule: amount() > 0 -> accept;`.
- **Шаги:** загрузить платёж с ip и email1; проверить платёж с тем же ip и email2; загрузить его и проверить с email3.
- **Ожидание:** результат меняется на `fatal` при превышении порога уникальных email.

### PAY-FL-07. `countSuccess`, `countError`, `countPending`
- **Предусловия:** четыре party с шаблонами `countSuccess("card_token", 30, days, "party_id") > 2`,
  `countError("card_token", 30, days) > 2` (два раза), `countPending("card_token", 30, days) > 1` → `decline`, иначе `accept`.
  У `countError` перед `group_by` стоит необязательный код ошибки, поэтому правило без группировки.
- **Шаги:** party countSuccess: 3 `captured`, 1 `failed`, 1 `pending`; party countError: 3 `failed`, 1 `captured`, 1 `pending`;
  party countPending: 4 `pending` (функция считает pending-события минус завершённые операции карты);
  контрольная party countError: 3 `captured`, 1 `failed`, 1 `pending`. Проверить платёж в каждой party.
- **Ожидание:** первые три — `fatal`, контрольная — `low`.

### PAY-FL-08. `sumSuccess`, `sumError`
- **Шаги:** пять party+shop: `sumSuccess(card) > 15000` с 2×10000 `captured` и 30000 `failed`; то же с 30000 `failed` и
  10000 `captured`; `sumError(card, 30, days, "k6_err") > 15000` с 2×10000 `failed` кода `k6_err`; то же с кодом
  `k6_other`; `sumError` без кода с ошибками обоих кодов.
- **Ожидание:** `fatal`, `low`, `fatal`, `low`, `fatal`: суммируются только нужные статусы и коды.

### PAY-FL-09. `countRefund`, `sumRefund`
- **Предусловия:** шаблон `rule: countRefund("card_token", 30, days) > 0 -> decline; rule: amount() > 0 -> accept;`.
- **Шаги:** проверить платёж; загрузить через `POST /refunds` успешный возврат по card_token кейса; проверить снова.
- **Ожидание:** `low` → `fatal`. Аналогично для `sumRefund` с порогом по сумме.

### PAY-FL-10. `countChargeback`, `sumChargeback`
- Аналогично PAY-FL-09 через `POST /chargebacks`. **Ожидание:** `low` → `fatal`.

### PAY-FL-11. Идентичность клиента
- **Предусловия:** шаблон `rule: count("user_id", 1, hours, "party_id") > 2 -> decline; rule: amount() > 0 -> accept;` на две party.
- **Шаги:** загрузить платежи с одним `user_id` (и `customerId`: контракт проверки требует его вместе с `user_id`) в party A
  до порога; проверить платёж этого user_id в party A и в party B; проверить платёж с `user_id` без `customerId`.
- **Ожидание:** party A — `fatal`; party B — `low`. Ответ на `user_id` без `customerId` — наблюдение (на обеих версиях 500).

### PAY-FL-12. Чёрный список
- **Предусловия:** шаблон `rule: inBlackList("card_token") -> decline; rule: amount() > 0 -> accept;`.
- **Шаги:** проверить платёж; добавить card_token в чёрный список party+shop; проверить; удалить строку; проверить.
- **Ожидание:** `low` → `fatal` → `low`.

### PAY-FL-13. Белый список
- **Предусловия:** шаблон `rule: inWhiteList("email") -> accept;`.
- **Шаги:** проверить платёж; добавить email в белый список; проверить.
- **Ожидание:** `high` → `low`.

### PAY-FL-14. Серый список с лимитом
- **Предусловия:** шаблон `rule: inGreyList("card_token") -> accept;`.
- **Шаги:** добавить card_token в серый список с `count=1` и окном «сейчас … сейчас+1 день»; проверить платёж;
  загрузить его в историю; проверить следующий платёж.
- **Ожидание:** первая проверка — `low`; после исчерпания лимита правило не срабатывает — `high`.

### PAY-FL-15. Именованный список
- **Статус:** не выполняется: `POST /payments-lists` (и CSV) не принимает тип `naming` (500 `UnknownEventException` на
  обеих версиях). `inList` проверяется для выплат в PO-FL-14.

### PAY-FL-16. Доверенная карта
- **Предусловия:** шаблон `rule: isTrusted(paymentsConditions(condition("RUB", 1, 1, 1))) -> accept;`.
  Новая версия удалила формы `isTrusted(paymentsConditions(...))`/`withdrawalsConditions(...)` из грамматики
  (`e01057aa`): кейс фиксирует несовместимость существующих шаблонов. Несовместимость принята: в продакшен-шаблонах
  `isTrusted` не используется (решение от 2026-11-17), поэтому падение на новой версии ожидаемое.
- **Шаги:** проверить платёж; загрузить успешные платежи по card_token, достаточные для условия; проверить снова (с ожиданием).
- **Ожидание:** `high` → `low`.

### PAY-FL-17. Гео
- **Шаги:** шаблон с правилами `countryBy("country_bank") = "RUS"/"RU"`, `in(countryBy("ip"), …)`,
  `countryBy("ip") = "UNKNOWN"`; платежи с частным ip `10.0.0.1`, с публичным `1.208.61.253`, с картой `RUS`.
- **Ожидание:** частный ip — `accept/ip_unknown`. Наблюдения: страна банка (`decline/bank_rus` на обеих версиях: код
  передаётся как alpha-3), публичный ip (`high_risk/ip_in_list`).

### PAY-FL-18. `isRecurrent`, `isMobile`
- **Шаги:** шаблон `recurrent: isRecurrent() -> decline; mobile: isMobile() -> highRisk; other -> accept`; платежи с
  `payerType` `recurrent` и `mobile`, с `tokenMobileProvider`, обычный.
- **Ожидание:** обычный — `accept/other`. Наблюдения: через REST все три дают `accept/other` на обеих версиях —
  fraudbusters-api всегда передаёт `Payer.payment_resource` и не передаёт провайдера токена, а fraudbusters берёт оба
  признака из Thrift-контекста. Проверить `isRecurrent`/`isMobile` можно только через Thrift `InspectPayment`.

### PAY-FL-19. Каскад: шаблон party и party+shop
- **Шаги и ожидание:**
  1. Только шаблон party+shop `accept` → `low`.
  2. Шаблон party `decline` и шаблон party+shop `accept` → `fatal` (шаблон party раньше в каскаде).
  3. Шаблон party без сработавших правил и шаблон party+shop `accept` → `low`.
  4. Шаблон party `decline`, проверка платежа другого shop этой party → `fatal`.

### PAY-FL-20. Каскад: группа и шаблон, приоритеты в группе
- **Шаги и ожидание:**
  1. Группа party из шаблона `decline` и шаблон party `accept` → `fatal`.
  2. Группа party+shop `accept` и шаблон party `decline` → `low`.
  3. Группа из двух шаблонов с разными приоритетами (`accept` и `decline`) → решение шаблона с приоритетом,
     который применяется первым. Порядок приоритетов фиксируется по старой версии.
  4. Шаблон с первым приоритетом без сработавших правил → применяется следующий шаблон группы.

### PAY-FL-21. Каскад: глобальный шаблон (глобальный)
- **Статус:** не выполняется. REST API management не может создать глобальную привязку платежей:
  `PaymentReference` не содержит `isGlobal`, а конвертер не выставляет `is_global` в команде.
  Кейс вернётся, когда такая возможность появится в API.
- **Предусловия:** глобальный шаблон `rule: in("party_id", "<party кейса>") -> decline;`.
- **Шаги:** привязать к party+shop шаблон `accept`; проверить платёж party кейса и платёж другой party.
- **Ожидание:** party кейса — `fatal` (глобальный шаблон первый в каскаде); другая party — по своей привязке.
  После кейса исходная глобальная привязка восстановлена.

### PAY-FL-22. Каскад: `notify` не останавливает проверку
- **Шаги:** шаблон party `rule: amount() > 0 -> notify;`, шаблон party+shop `rule: amount() > 0 -> decline;`; проверить.
- **Ожидание:** `fatal`. Если шаблон party+shop не привязан — `high`.

### PAY-FL-23. Привязка по умолчанию для нового магазина
- **Предусловия:** шаблон `decline`; привязка по умолчанию на него для party кейса (`POST /payments-references/default`).
- **Шаги:** проверить платёж нового магазина этой party; дождаться автоматической привязки
  (`GET /payments-references/filter`); проверить снова.
- **Ожидание:** первая проверка — `high`; появляется привязка party+shop на шаблон по умолчанию; затем `fatal`.
  Привязку создаёт management по событию `unknown_initiating_entity` (fraudbusters, `default.template.enable`).

### PAY-FL-24. Изменение и удаление шаблона, удаление привязки
- **Шаги:** привязать шаблон `accept` → проверить; заменить текст шаблона на `decline` → проверить;
  удалить привязку → проверить; привязать снова и удалить шаблон → проверить.
- **Ожидание:** `low` → `fatal` → `high` → `high`; каждое изменение применяется в пределах `WAIT_TIMEOUT_MS`.

### PAY-FL-25. Проверка пользователя
- **Шаги:** три магазина одной party: `userId() = "<user>" -> decline`; правило, которое не срабатывает;
  `amount() > 0 -> decline`. `POST /inspect-user` с этим пользователем и тремя магазинами.
- **Ожидание:** заблокирован магазин с правилом по `userId`, магазин без срабатывания — нет. Наблюдение: магазин с
  правилом по сумме не блокируется — модель проверки пользователя не содержит суммы, ip и fingerprint, правило падает
  с NPE и становится уведомлением. Локально нужен `fraudbusters.service.user.inspector.url` в `infra/compose.yaml`.

### PAY-FL-26. Загрузка fraud-платежей
- **Статус:** покрыт PAY-API-13 (загрузка fraud-платежей через CSV и fraudbusters-api, поиск в истории).

### PAY-FL-27. Шаблон в форме боевого
Шаблон повторяет конструкции рабочего платёжного шаблона с другими именами и значениями
(данные: [PAY-FL-27](data/payments/PAY-FL-27.js)): белый и чёрный списки по `email`,
`fingerprint`, `ip`; `like` с экранированной точкой; правила с одинаковыми именами и два правила в одной строке;
`and`/`or`/`not` в нижнем регистре и вложенные скобки; `trust` по `countSuccess` + `countError` с кодом ошибки и по
`sumSuccess`; `accept` по месячной сумме; `decline` по `countPending` в минутах и по `countError` за сутки;
исключение `not in("email", "UNKNOWN")`; один код ошибки в двух написаниях (`card_expired: k6` и `card_expired:k6`).
- **Предусловия:** party+shop с шаблоном; строка белого списка `FINGERPRINT`, строка чёрного списка `IP`.
  История загружается до проверок, у каждого сценария свой email.
- **Ожидание** (решение в результатах проверки / сработавшее правило):

| Сценарий | Решение | Правило |
| --- | --- | --- |
| fingerprint в белом списке, email заблокированного домена | `accept` | `allow_list` |
| ip в чёрном списке | `decline` | `deny_list` |
| email `@blocked-mail.test`, `@blocked-mail-alt.test` | `decline` | `deny_domain` |
| email `@blocked-mailxtest` (точка экранирована) | нет решения | — |
| новый email без истории | нет решения | — |
| 5 успешных платежей | `trust` | `loyal_by_count` |
| то же, сумма выше лимита `amount() <= 500000` | нет решения | — |
| 5 успешных и 4 ошибки `card_expired: k6` | `decline` | `expired_day` |
| 5 успешных и 4 ошибки с другим кодом | `trust` | `loyal_by_count` |
| платежи без email (хранятся как `UNKNOWN`) | нет решения | — |
| 2 успешных по 200000 | `trust` | `loyal_by_sum` |
| то же, сумма выше лимита | `accept` | `regular_monthly` |
| 5 платежей `pending` за 5 минут | `decline` | `pending_burst` |

- **Наблюдения:** ответ `/inspect-payment` на `trust` (на обеих версиях 500: в REST-контракте нет `trusted`);
  решение для email, переданного как `UNKNOWN` (fraudbusters приводит email к нижнему регистру, поэтому
  исключение не срабатывает, решение `trust`); ошибки `card_expired:k6` правилом с кодом `card_expired: k6`
  не считаются (код сравнивается точно).

### PAY-FL-28. Результаты правил: 3ds, *AndNotify, уведомления, catch
- **Шаги:** шесть party+shop с шаблонами: `-> 3ds`; `-> acceptAndNotify`; `-> declineAndNotify`; два `notify` и затем
  `decline`; `rand(0) > 1 -> decline catch: highRisk` и запасное `accept`; то же без `catch`. Проверить шаблон с
  `catch: decline`.
- **Ожидание** (результат / правило / ответ REST): `three_ds/ask_3ds/high`; `accept_and_notify/accept_notify/low`;
  `decline_and_notify/decline_notify/fatal`; `decline/final_decline/fatal`; `high_risk/broken/high` (ошибка
  `rand(0)` уходит в `catch`); без `catch` ошибочное правило становится уведомлением и решает `accept/fallback/low`.
  `catch: decline` отклоняется валидацией: грамматика допускает в `catch` только `trust`, `accept`, `3ds`,
  `highRisk`, `notify` (пример `catch: decline` в документации Fraudo неверен). Наблюдение: `notificationsRule`
  в результатах проверки пустой на обеих версиях.

### PAY-FL-29. Функции и операторы
- **Шаги:** шаблон с правилами `userId() = …`, `rand(1) < 1`, дробные границы `amount() > 7101.5`,
  `(not (…)) and (… or …)`, `!=`, `like("card_token", ".*-like-card$")`, `paymentSystem()`, `cardCategory()`,
  строка-комментарий `#`; отдельный шаблон `not amount() < 7200 and amount() = 7201`.
- **Ожидание:** каждое правило срабатывает на своём платеже (решение и имя правила по результатам проверки);
  `like` требует полного совпадения. Наблюдения: `paymentSystem()` и `cardCategory()` через REST не заполнены
  (`accept/other`, `decline/category_unknown`); **`not` относится ко всему остатку выражения**: `not A and B` — это
  `not (A and B)`, правило без скобок срабатывает и на 7300, и на 5000.

### PAY-FL-30. Окна времени
- **Шаги:** `count("card_token", …)` с окнами `10, minutes`, `1, calendar_days`, `1, calendar_months`,
  `24, 1, hours`, `30, 1, days`, `1` (без единицы), `2, 1` (смещение без единицы); по 3 платежа карты, затем проверка.
- **Ожидание:** minutes, calendar_days, calendar_months — `fatal`; окна со смещением исключают свежую историю — `low`
  (порог `> 1`: `count` прибавляет проверяемый платёж). Наблюдения: без единицы — `fatal`, смещение без единицы — `low`.

### PAY-FL-31. Агрегаты по полям
- **Шаги:** `count("<поле>", 1, days, "party_id", "shop_id") > 2` для email, ip, fingerprint, phone, bin, masked_pan,
  country_bank; по 3 платежа с одинаковым значением поля.
- **Ожидание:** email, ip, fingerprint, phone, bin — `fatal`. Наблюдения: `masked_pan` и `country_bank` — `low` на обеих
  версиях. `masked_pan` нет в колонках истории платежей: правило падает (`UnknownFieldException`), без `catch` становится
  уведомлением, с `catch: highRisk` — `high`; валидация такой шаблон пропускает.

### PAY-FL-32. Thrift IsBlacklisted
- **Шаги:** строка чёрного списка `CARD_TOKEN` для party+shop; `InspectorProxySrv.IsBlacklisted` для этой карты,
  другой карты и другого магазина.
- **Ожидание:** `true`, `false`, `false`.

## Платежи: API управления и загрузки (PAY-API)

Пути указаны относительно `/fb-management/v1`. Пагинация проверяется по инвариантам [PG](#инварианты-пагинации-pg).

### PAY-API-01. Валидация шаблона
- **Шаги:** `POST /payments-templates/validate` с корректным шаблоном и с синтаксической ошибкой.
- **Ожидание:** для корректного — список ошибок пуст; для ошибочного — ошибка с непустым текстом.

### PAY-API-02. Создание, обновление, удаление шаблона
- **Шаги:** `POST /payments-templates` с корректным шаблоном; повторно с тем же id и новым текстом;
  с ошибкой в тексте; `DELETE /payments-templates/{id}`.
- **Ожидание:** ответ содержит id и текст; повторная запись заменяет текст; ошибочный шаблон возвращает поле `errors`
  и не появляется в поиске; после удаления шаблона нет в поиске.

### PAY-API-03. Поиск шаблонов и пагинация
- **Предусловия:** 7 шаблонов кейса.
- **Шаги:** `GET /payments-templates/filter` с `searchValue=<префикс кейса>`, `size=3`, `sortOrder` = `ASC`, `DESC`, `DEFAULT`, без `sortOrder`.
- **Ожидание:** PG-1…PG-7; `count` равен 7; `DEFAULT` и отсутствие параметра дают порядок `ASC`;
  `searchValue` ищет по подстроке id.

### PAY-API-04. Имена шаблонов
- **Шаги:** `GET /payments-templates/names` с шаблоном имени кейса.
- **Ожидание:** возвращены все шаблоны кейса и только они.

### PAY-API-05. Привязки шаблонов
- **Шаги:** `POST /payments-references` для party и для party+shop; `GET /payments-references/filter` (поиск, пагинация);
  `DELETE /payments-references/{id}`; привязка на несуществующий шаблон.
- **Ожидание:** привязки найдены с полями из запроса; PG-1…PG-7; после удаления не найдены.
  Ответ на несуществующий шаблон фиксируется по старой версии и должен совпасть на новой (422).
- **Особенности API:** `searchValue` сравнивается через `LIKE` без `%`, то есть ищется точное значение id шаблона,
  party или shop; поиск по префиксу — наблюдение. Ответ на сохранение содержит ключ привязки `party_shop`,
  удалять нужно по `id` из поиска (UUID). Курсор следующей страницы: `lastId` + `sortFieldValue` (id шаблона).

### PAY-API-06. Привязки по умолчанию
- **Шаги:** три привязки по умолчанию на один шаблон (`POST /payments-references/default`), поиск по id шаблона
  (`GET /payments-references/default/filter`), удаление одной (`DELETE /payments-references/default/{id}`).
- **Ожидание:** найдены три привязки с party из запросов, после удаления — две.

### PAY-API-07. Группы шаблонов
- **Шаги:** `POST /payments-groups` с двумя шаблонами и приоритетами; `GET /payments-groups/{id}`;
  `GET /payments-groups/filter`; `DELETE /payments-groups/{id}`.
- **Ожидание:** группа возвращается с теми же шаблонами и приоритетами; найдена поиском; после удаления отсутствует.

### PAY-API-08. Привязки групп
- **Шаги:** `POST /payments-groups/{id}/references` для party и party+shop; `GET /payments-groups/references/filter`;
  `DELETE /payments-groups/{id}/references/remove`.
- **Ожидание:** привязки найдены; PG-1, PG-2, PG-4, PG-5; после удаления не найдены.
  Поиск, как и в PAY-API-05, по точному значению: кейс ищет по id группы.

### PAY-API-09. Списки: строки, имена, поиск
- **Шаги:** добавить строки типов `white`, `black`, `grey` (с `countInfo`); строку `naming` (наблюдение: 500);
  `GET /payments-lists/availableListNames`; `GET /payments-lists/currentListNames?listType=…`;
  `GET /payments-lists/filter` по `listType`, `listNames`, `searchValue`; удалить строку по id.
- **Ожидание:** строки найдены с типом, именем, значением, party и shop; у серой строки сохранены
  `count`, `startCountTime`, `endCountTime`; имена текущих списков содержат добавленные; PG-1…PG-7;
  удалённая строка не находится.

### PAY-API-10. Списки: CSV
- **Шаги:** `POST /payments-lists/csv` (`listType=black`, файл `party_id,shop_id,list_name,value`, 5 строк);
  CSV серого списка с `count_info_*`; `POST /payments-lists/remove/csv` с тем же чёрным файлом.
- **Ожидание:** найдено 5 строк, после удаления — 0. Наблюдение: CSV серого списка отклоняется (500 на обеих версиях:
  парсер проверяет время `LocalDateTime` с `ISO_INSTANT`, такой разбор никогда не проходит).

### PAY-API-11. Списки: кандидаты
- **Шаги:** `POST /payments-lists/candidates` с чарджбэком (email, ip, fingerprint клиента); найти кандидатов по
  значению (`GET /payments-lists/candidates`, поиск по значению, имени списка или batch id — точное совпадение);
  `GET /payments-lists/candidates-batches`; одобрить кандидатов email и fingerprint
  (`POST /payments-lists/candidates/approved`).
- **Ожидание:** три кандидата чёрного списка с party и shop чарджбэка; одобренный email появляется в чёрном списке
  `EMAIL`, неодобренный ip — нет. Кандидаты уникальны по значению: в каждом прогоне свой ip.
  Наблюдение: кандидат по fingerprint создаётся в списке **`FINGERPRRINT`** (опечатка в `CandidateListName`) и после
  одобрения попадает в список, который `inBlackList("fingerprint")` не читает.

### PAY-API-12. История платежей: фильтры и пагинация
- **Предусловия:** 12 платежей кейса с разными статусами, email, card_token, fingerprint, terminal, maskedPan,
  providerCountry, customerId, userId, invoiceId.
- **Шаги:** `GET /payments-historical-data/payments-info` с каждым фильтром отдельно; с узким окном `from`/`to`;
  с `sortBy`/`sortOrder`; постранично по `continuationId` с `size=5`.
- **Ожидание:** каждый фильтр возвращает только подходящие платежи кейса; окно отсекает платежи вне него;
  PG-1…PG-6 для `continuationId`; несуществующее значение фильтра даёт пустой результат.

### PAY-API-13. История возвратов, чарджбэков, fraud-платежей
- **Шаги:** загрузить 3 платежа, по возврату и чарджбэку на каждый через fraudbusters-api; два fraud-платежа
  загрузить CSV-файлом через `POST /payments-load-data/fraud-payments` (`id,event_time,fraud_type,comment`,
  время `yyyy-MM-dd HH:mm:ss`), третий — через `POST /fraud-payments` fraudbusters-api.
- **Ожидание:** возвраты и чарджбэки найдены по party; fraud-платежи из CSV найдены по `paymentId`.
  Наблюдения: тип и комментарий fraud-платежа в ответе (на обеих версиях пустые) и сохранение fraud-платежа
  из fraudbusters-api (не сохраняется: REST требует ISO-время, fraudbusters разбирает `yyyy-MM-dd HH:mm:ss`).

### PAY-API-14. История результатов проверки
- **Шаги:** выполнить проверки из PAY-FL-01 и PAY-FL-02; искать в `/payments-historical-data/inspect-results` по `paymentId`, `template`.
- **Ожидание:** найдены результаты с решением, шаблоном и правилом; фильтр `template` отсекает чужие проверки.
  Платёж из fraudbusters-api сохраняется в результатах как `<id>.<id>` (invoiceId.paymentId), фильтр `paymentId`
  ищет это составное значение. Наблюдения: фильтр `invoiceId` не сужает выборку, `paymentId=<id>` ничего не находит.

### PAY-API-15. Наборы данных и применение правила к истории
- **Шаги:** создать набор данных из 4 платежей одной карты (5000, 5000, 150000, 5000) (`/payments-data-set/data-sets`);
  получить его и найти по имени с окном `from`/`to`; применить к строкам набора шаблон
  `big_amount: amount() >= 100000 -> decline; card_velocity: count("card_token", 1, days) > 2 -> decline; other -> accept`
  (`applyRuleOnHistoricalDataSet`, записи — строки сохранённого набора с их числовыми id); получить проверенный
  набор по id набора данных (`/payments-data-set/checked-data-sets/{id}`); удалить набор.
- **Ожидание:** 4 проверенные строки; платёж 150000 — `decline/big_amount`, остальные — `accept/other` или
  `decline/card_velocity`. Наблюдения: решения по строкам (на обеих версиях `card_velocity` не срабатывает),
  фильтр без `from`/`to` (500: параметры объявлены необязательными, сервис их требует), получение проверки по id,
  который вернул `applyRuleOnHistoricalDataSet` (это id результата проверки, а `GET` ищет по id набора данных и
  отдаёт проверку другого набора), получение удалённого набора (500).

### PAY-API-16. Эмуляция каскада
- **Шаги:** настроить группу party и шаблон party+shop; запросить `GET /payments-emulations` для party+shop.
- **Ожидание:** возвращены все три шаблона, шаблон party раньше шаблона party+shop. Наблюдение: порядок
  (на обеих версиях: группа, party, party+shop).

### PAY-API-17. Аналитика
- **Шаги:** в истории party+shop: 3 платежа `captured`, 2 по 150000 `failed` с кодом
  `no_route_found:risk_score_is_too_high` (так аналитика определяет заблокированный антифродом платёж), 1 `failed` с
  другим кодом. Запросить `/analytics/fraud-payments/blocked/count`, `…/blocked/count/ratio`, `…/blocked/sum`,
  `…/count`, `…/results/summary`, `…/scores/split-count/ratio?splitUnit=hour`, `/analytics/currencies`
  (`fromTime`/`toTime` — `yyyy-MM-dd HH:mm:ss`, `merchantId` — party).
- **Ожидание:** заблокировано 2, доля 2/6, сумма 300000 в минорных или 3000 в основных единицах, остальные запросы — 200,
  `currencies` содержит `RUB`. Наблюдения: сумма возвращается в основных единицах (3000); `fraud-payments/count`
  считает все платежи (6); `results/summary` пустой без результатов проверки.

### PAY-API-18. Аудит
- **Шаги:** справочники `GET /audit/commandTypes`, `/audit/objectTypes`; создать шаблон и привязку, удалить шаблон;
  `GET /audit/filter` с окном, типами команд и объектов (поиск возможен только по инициатору, записи кейса
  отбираются по тексту объекта).
- **Ожидание:** есть `template:CREATE`, `template:DELETE`, `reference:CREATE` с инициатором и временем.
  Наблюдение: справочник типов объектов (на `after-payouts` добавлены `payout_*`).

### PAY-API-19. Некорректные параметры management
- **Шаги:** запросы без обязательных `from`/`to`; `sortOrder=FOO`; неизвестный `listType`; `size=0`, `size=-1`, без `size`.
- **Ожидание:** ответы на старой и новой версии совпадают; 5xx допускается только для известного дефекта:
  `sortOrder=FOO`, неизвестный `listType` и `size=-1` доходят до DAO и отвечают 500 на обеих версиях.

### PAY-API-20. Загрузка данных в fraudbusters-api
- **Шаги:** `POST /payments`, `/refunds`, `/chargebacks`, `/fraud-payments` с пустым списком и с записью без обязательных полей.
- **Ожидание:** пустой список — 201; запись без обязательных полей — 400.

## Выплаты: сквозные сценарии (PO-FL)

Проверка выплаты — Thrift `InspectorProxySrv.inspectWithdrawal(WithdrawalContext)`.
Загрузка истории — `POST /withdrawals` в fraudbusters-api со статусами `pending`, `succeeded`, `failed`.
Шаблон привязывается к party+wallet кейса, если не сказано иное.

### PO-FL-01. Выплата без риска: проверка, история
- **Предусловия:** шаблон выплаты `rule: amount() >= 100000 -> decline; rule: amount() < 100000 -> accept;`.
- **Шаги:** проверить выплату на 5000; загрузить её со статусом `succeeded`; найти в
  `GET /payouts-historical-data/withdrawals` по `withdrawalId`; проверить выплату на 150000.
- **Ожидание:** `low`; запись найдена с party, wallet, суммой и статусом `succeeded`; вторая проверка — `fatal`.

### PO-FL-02. Нет привязки и неполный контекст
- **Шаги:** проверить выплату party+wallet без привязок; проверить выплату без `wallet_id`; без `party_id`.
- **Ожидание:** без привязок — `high`; пустые `party_id`/`wallet_id` — исключение `InvalidRequest`
  с текстом `party_id and wallet_id are required for withdrawal inspection`. Отсутствующее поле Thrift отклоняется
  ещё на уровне протокола (`Required field 'id' was not present`).

### PO-FL-03. Каскад шаблонов и групп выплат
Порядок: глобальный шаблон → глобальная группа → группа party → группа party+wallet → шаблон party → шаблон party+wallet.
- **Шаги и ожидание:**
  1. Только шаблон party+wallet `accept` → `low`.
  2. Шаблон party `decline` и шаблон party+wallet `accept` → `fatal`.
  3. Группа party+wallet `accept` и шаблон party `decline` → `low`.
  4. Группа party `decline` и группа party+wallet `accept` → `fatal`.
  5. Шаблон party без сработавших правил и шаблон party+wallet `accept` → `low`.
  6. Шаблон party `notify` и шаблон party+wallet `decline` → `fatal`.
  7. Шаблон party `decline`, выплата другого wallet этой party → `fatal`.

### PO-FL-04. Глобальная привязка выплат (глобальный)
- **Шаги:** глобальная привязка (`isGlobal=true`) на шаблон `rule: in("party_id", "<party кейса>") -> decline;`;
  затем глобальная группа с таким же шаблоном вместо глобальной привязки.
- **Ожидание:** выплата party кейса — `fatal` в обоих вариантах; другие party не затронуты;
  исходная глобальная конфигурация восстановлена.

### PO-FL-05. Списки выплат и их область действия
- **Предусловия:** шаблон `rule: inBlackList("card_token") -> decline; rule: amount() > 0 -> accept;` на party A и party B.
- **Шаги:**
  1. Проверить выплату по card_token кейса в party A.
  2. Добавить card_token в чёрный список с областью party A + wallet; проверить в этом wallet, в другом wallet party A, в party B.
  3. Добавить строку с областью party A без wallet; повторить проверки.
  4. Аналогично для белого списка (`inWhiteList("email") -> accept`) и серого списка с `count=1`.
- **Ожидание:** шаг 1 — `low`; строка срабатывает только в своей области; party B не затронута;
  белый список даёт `low`, серый — `low` до исчерпания лимита.

### PO-FL-06. `countPayouts`, `sumPayouts`
- **Предусловия:** шаблон `rule: countPayouts("card_token", 24, hours, "party_id") > 2 -> decline; rule: amount() > 0 -> accept;`.
- **Шаги:** загружать выплаты по card_token кейса со статусами `succeeded` и `failed`, проверяя после каждой;
  загрузить выплату `pending`.
- **Ожидание:** учитываются только завершённые выплаты (`succeeded` и `failed`), `pending` не влияет;
  проверяемая выплата в счётчик не входит (наблюдение: `> 2` срабатывает после третьей завершённой);
  после порога — `fatal`. Аналогично для `sumPayouts` с порогом по сумме.

### PO-FL-07. `countPayoutsSuccess`, `sumPayoutsSuccess`
- **Ожидание:** учитываются только `succeeded`; выплаты `failed` не меняют результат.

### PO-FL-08. `countPayoutsError`, `sumPayoutsError` с кодом ошибки
- **Предусловия:** шаблон `rule: countPayoutsError("card_token", 24, hours, "<код>") > 1 -> decline; …`.
- **Шаги:** загрузить `failed` выплаты с кодом `<код>` и с другим кодом.
- **Ожидание:** считаются только ошибки с указанным кодом; без кода в правиле — все ошибки.

### PO-FL-09. `uniquePayouts`
- **Шаги:** выплаты одного user_id на разные card_token; правило на число уникальных card_token.
- **Ожидание:** `fatal` при превышении числа уникальных значений.

### PO-FL-10. Запрещённые конструкции в шаблоне выплаты
- **Шаги:** валидировать и сохранить шаблоны выплаты с `isTrusted()`, `isMobile()`, `isRecurrent()`, результатом `3ds`
  (в правиле и в `catch`), полем `shop_id` в `count`/`in`/`inBlackList`.
- **Ожидание:** каждая конструкция отклоняется ошибкой валидации (`PayoutTemplateValidator`), шаблон не сохраняется.
  Дополнительно: `count("wallet_id", …)` (поля нет в истории платежей), `countryBy("email")`; `3ds` в шаблоне платежа
  остаётся допустимым.

### PO-FL-12. Функции истории платежей в правиле выплаты
- **Шаги:** шаблоны выплат с `count("card_token", 1, days, "party_id") > 1`, `sum("email", 1, days, "party_id") > 15000`,
  `unique("user_id", "card_token", 1, days) > 1` → `decline`; загрузить по два успешных платежа с теми же значениями.
- **Ожидание:** до платежей — `low`, после — `fatal` во всех трёх вариантах.

### PO-FL-13. Группировка истории выплат по `wallet_id`
- **Шаги:** шаблон party `countPayouts("card_token", 24, hours, "wallet_id") > 1 -> decline`; две успешные выплаты
  карты в wallet A.
- **Ожидание:** wallet A — `fatal`, та же карта в wallet B той же party — `low`.

### PO-FL-11. Изменение и удаление шаблона выплаты
- Аналогично PAY-FL-24 для выплат. **Ожидание:** `low` → `fatal` → `high` → `high`.

### PO-FL-14. Списки выплат по полям, именованный список, countryBy
- **Шаги:** шаблон выплаты с `inBlackList("party_id"|"wallet_id"|"currency"|"user_id")`, `inList("<список>", "email")`,
  `countryBy("ip") = "UNKNOWN"`; строки списков `PARTY_ID`, `WALLET_ID`, `CURRENCY`, `USER_ID` и именованного списка.
- **Ожидание:** party, wallet, валюта `USD`, пользователь — `fatal`; email из именованного списка — `high`; частный ip —
  `high`; выплата без совпадений — `low`.

## Выплаты: API управления и загрузки (PO-API)

### PO-API-01. Валидация шаблона выплаты
- **Шаги:** `POST /payouts-templates/validate` с корректным шаблоном, с ошибкой синтаксиса,
  с функциями истории выплат.
- **Ожидание:** корректный шаблон и шаблон с функциями выплат — без ошибок; ошибочный — ошибка с текстом.

### PO-API-02. Создание, обновление, удаление шаблона выплаты
- **Ожидание:** как PAY-API-02; ошибочный шаблон возвращает поле `errors` и не сохраняется.

### PO-API-03. Поиск шаблонов выплат и пагинация
- **Шаги:** `GET /payouts-templates/filter` с `searchValue`, `lastId`, `size`, `sortOrder`.
- **Ожидание:** PG-1…PG-7; `count` равен числу шаблонов кейса; шаблоны платежей в выдачу не попадают.

### PO-API-04. Имена шаблонов выплат
- **Ожидание:** `GET /payouts-templates/names` возвращает шаблоны выплат кейса и не возвращает шаблоны платежей.

### PO-API-05. Привязки шаблонов выплат
- **Шаги:** `POST /payouts-references` для party, party+wallet, глобальной (`isGlobal=true`), wallet без party;
  `GET /payouts-references/filter` с `isGlobal`, `searchValue`, пагинацией; `DELETE /payouts-references`
  по `partyId`/`walletId`/`isGlobal`; удаление несуществующей привязки.
- **Ожидание:** wallet без party — 400; фильтр `isGlobal` разделяет глобальные и обычные; PG-1…PG-7;
  удаление несуществующей — 404.

### PO-API-06. Группы шаблонов выплат
- **Шаги:** `POST /payouts-groups` с приоритетами и без `groupId`; `GET /payouts-groups/{id}` для существующей
  и несуществующей; `GET /payouts-groups/filter?searchValue=`; `DELETE`.
- **Ожидание:** без `groupId` — 400; несуществующая группа — 404; поиск возвращает группы кейса и `count`,
  равный их числу (у поиска групп нет пагинации).

### PO-API-07. Привязки групп выплат
- **Шаги:** `POST /payouts-groups/{id}/references` для party и party+wallet; `GET /payouts-groups/references/filter`;
  `DELETE /payouts-groups/{id}/references?partyId=&walletId=`.
- **Ожидание:** привязки найдены; PG-1…PG-7; после удаления не найдены.

### PO-API-08. Списки выплат
- **Шаги:** `POST /payouts-lists` со строками `white`, `black`, `grey` (с `countInfo`), `naming`;
  строка с именем списка вне доступных; без `value`; с wallet без party;
  `GET /payouts-lists/availableListNames`, `currentListNames`, `filter`; `DELETE /payouts-lists/{id}` для существующей
  и несуществующей строки.
- **Ожидание:** доступные имена: `EMAIL, IP, FINGERPRINT, USER_ID, CARD_TOKEN, BIN, LAST_DIGITS, MASKED_PAN, PHONE,
  COUNTRY_BANK, CURRENCY, PARTY_ID, WALLET_ID`; список вне доступных (кроме `naming`), пустое значение,
  wallet без party — 400; PG-1…PG-7; удаление несуществующей — 404; строки платежей в выдачу не попадают.

### PO-API-09. История выплат
- **Предусловия:** 12 выплат кейса с разными party, wallet, статусами, email, card_token, fingerprint, terminal,
  maskedPan, providerCountry, customerId, userId.
- **Шаги:**
  1. `GET /payouts-historical-data/withdrawals` с каждым фильтром.
  2. Загрузить выплату со статусом `pending`, затем ту же со `succeeded`; искать по `withdrawalId`,
     по `withdrawalStatus=pending` и `succeeded`.
  3. Узкое окно `from`/`to`; `sortBy`/`sortOrder`; постранично по `continuationId` с `size=5`.
- **Ожидание:** каждый фильтр возвращает только подходящие выплаты; выплата возвращается один раз в последнем
  состоянии (`succeeded`), фильтр `pending` её не находит; PG-1…PG-6.

### PO-API-10. Аудит команд выплат
- **Шаги:** создать шаблон, привязку, группу, привязку группы и строку чёрного списка выплат; удалить шаблон;
  `GET /audit/filter` по типам `payout_*`.
- **Ожидание:** есть `payout_template:CREATE/DELETE`, `payout_reference:CREATE`, `payout_group:CREATE`,
  `payout_group_reference:CREATE`, `payout_black:CREATE`.

### PO-API-11. Некорректные параметры API выплат
- **Шаги:** неизвестный `listType`; `sortOrder=FOO`; история без `from`/`to`; `size=0`, `size=-1`.
- **Ожидание:** 4xx, ни одного 5xx. На `after-payouts` `size=-1` отвечает 500 (`LIMIT must not be negative`):
  дефект API выплат, кейс красный.

### PO-API-12. Загрузка выплат в fraudbusters-api
- **Шаги:** `POST /withdrawals` с пустым списком, с записью без обязательных полей, с `walletId` без `partyId`.
- **Ожидание:** пустой список — 201; без обязательных полей — 400; поведение для wallet без party фиксируется
  первым прогоном и проверяется дальше.

## Жизненный цикл: перезапуск и обновление (LC)

`scripts/run-k6.sh lifecycle <версия> [<новая версия>]` выполняет набор `lifecycle` в два этапа с одним `RUN_ID`:
`prepare` создаёт конфигурацию и историю, затем сервисы перезапускаются (или переключаются на образы новой версии
без удаления данных), `verify` проверяет решения, ничего не меняя.

| ID | Подготовка | Проверка после перезапуска / обновления |
| --- | --- | --- |
| LC-01 | шаблон `amount() >= 100000 -> decline` на party+shop | 150000 — `fatal`, 5000 — `low` |
| LC-02 | группа с шаблоном `decline` на party | `fatal` |
| LC-03 | шаблон `inBlackList("card_token")` и строка чёрного списка | карта из списка — `fatal`, другая — `low` |
| LC-04 | `count("card_token", 30, days, "party_id") > 2` и 3 платежа в истории | `fatal` по истории в ClickHouse |
| LC-05 | (только с `before-payouts`) шаблон `isTrusted(paymentsConditions(...))` + `big: amount() >= 100000 -> decline` | наблюдения: шаблон остаётся в management; решение для 150000 |
| LC-06 | (только с `after-payouts`) шаблон и строка чёрного списка выплат | карта из списка — `fatal`, другая — `low` |

Результат LC-05 при обновлении: новая версия пишет при загрузке шаблона WARN `no viable alternative at input
'isTrusted(paymentsConditions'`, разбирает шаблон с восстановлением после ошибки и применяет оставшиеся правила
(решение `fatal` по `big`); правило с `isTrusted` молча выпадает.

## Платежи и выплаты вместе (MIX)

### MIX-01. Успешные выплаты влияют на решение по платежу
- **Предусловия:** шаблон платежа `rule: countPayoutsSuccess("user_id", 24, hours, "party_id") > 3 -> highRisk; rule: amount() > 0 -> accept;`.
- **Шаги:** проверить платёж user_id кейса; загрузить 4 успешные выплаты этого user_id в той же party; проверить платёж
  с ожиданием; загрузить выплаты этого user_id в другой party и проверить платёж там.
- **Ожидание:** `low` → `high`; выплаты другой party не влияют.

### MIX-02. Платежи влияют на решение по выплате
- **Предусловия:** шаблон выплаты `rule: countSuccess("user_id", 30, days, "party_id") < 1 -> decline; rule: amount() > 0 -> accept;`.
- **Шаги:** проверить выплату user_id без платежей; загрузить успешный платёж этого user_id; проверить выплату с ожиданием.
- **Ожидание:** `fatal` → `low`.

### MIX-03. Валидация функций выплат в шаблоне платежа
- **Шаги:** валидировать шаблон платежа с `countPayouts`, `sumPayoutsSuccess`, `countPayoutsError`.
- **Ожидание:** ошибок нет.

### MIX-04. Конфигурации платежей и выплат не пересекаются
- **Предусловия:** шаблоны платежа и выплаты с одним id и разными порогами суммы (1,5 и 1,6 млрд:
  пороги больше 2³¹ в правилах не срабатывают).
- **Шаги:** создать шаблон платежа и шаблон выплаты с одинаковым id; привязать к party кейса; добавить в чёрные списки
  платежей и выплат разные значения.
- **Ожидание:** поиск каждого типа возвращает только свой шаблон; проверка платежа использует шаблон платежа,
  проверка выплаты — шаблон выплаты; строка списка платежей не влияет на выплату и наоборот.

## Нагрузка (NF)

Нагрузка создаётся итерациями сквозных сценариев, а не отдельными HTTP-запросами. Состав трафика `payments`
по умолчанию: новый платёж без риска с загрузкой в историю — 70%, отказ по сумме — 10%, карта из чёрного списка — 10%,
карта над лимитом `count` — 10% (`SHARE_ACCEPT`, `SHARE_AMOUNT`, `SHARE_BLACK`, `SHARE_VELOCITY`). `mixed` добавляет
выплаты: новая выплата без риска с загрузкой в историю — 15%, выплата по карте из чёрного списка — 5%
(`SHARE_PAYOUT_ACCEPT`, `SHARE_PAYOUT_BLACK`), платежи — 70%. 5% итераций дополнительно ищут в истории
(`SHARE_HISTORY_SEARCH`). Каждая итерация проверяет ожидаемое решение.

Пороги всех нагрузочных профилей: `checks` = 100%, `flow_success` = 100%, `dropped_iterations` = 0,
`http_req_failed` = 0. Пороги p95 по этапам задаются после первого baseline (`P95_LIMIT_MS`, по умолчанию 60 с).

| ID | Профиль | Executor и параметры по умолчанию | Что проверяем |
| --- | --- | --- | --- |
| NF-01 | `smoke` | per-vu-iterations, 1 VU, 1 итерация каждого кейса профиля S | стек и сценарии работают |
| NF-02 | `baseline` | constant-arrival-rate, `RATE=10/с`, `DURATION=5m` | эталонные p95/p99 этапов |
| NF-03 | `load` | ramping-arrival-rate: разгон 1 мин до `TARGET_RATE` (2×`RATE`), плато `DURATION` 5 мин | целевая нагрузка без ошибок |
| NF-04 | `stress` | ступени `RATE` ×1, ×2, ×4, ×8 по `STEP_DURATION` (2 мин) | предел: ступень, на которой нарушен порог |
| NF-05 | `spike` | `RATE` 1 мин → `SPIKE_RATE` (10×`RATE`) за 30 с, 2 мин, возврат | ошибки во время всплеска и восстановление |
| NF-06 | `soak` | constant-arrival-rate `RATE`, `DURATION` (1h) | стабильность латентности на длительном прогоне |
| NF-07 | `load` (сценарий `reconfigure`, `RECONFIGURE_AT`=3m) | на плато в шаблон нагрузки добавляется правило для суммы 4242 | время применения изменения; трафик сохраняет решения |
| NF-08 | `baseline` на двух версиях | `tools/compare.mjs` сравнивает два `summary.json` | совпадение результатов проверок, p95 этапов не хуже допуска (по умолчанию +20%) |
| NF-09 | `load`, `soak` для `mixed` | платежи и выплаты в одном потоке | смешанный трафик без ошибок, платежи не хуже NF-03 |

Локальный стек (один брокер, одна партиция, один узел ClickHouse) не показывает ёмкость продакшена.
Числа используются для сравнения прогонов между собой.

## Инварианты пагинации (PG)

Для поиска с курсором (`lastId` или `continuationId`) кейс создаёт N записей, где N не кратно `size`,
и проходит все страницы.

| ID | Инвариант |
| --- | --- |
| PG-1 | Объединение страниц совпадает с множеством созданных записей кейса |
| PG-2 | Нет повторов между страницами |
| PG-3 | Порядок соответствует `sortOrder` внутри и между страницами |
| PG-4 | Каждая страница, кроме последней, содержит ровно `size` записей |
| PG-5 | Когда записей ровно `size`, следующая страница пустая |
| PG-6 | Запись, добавленная между запросами страниц, не вызывает повторов уже полученных записей |
| PG-7 | `count` в ответе равен общему числу записей, подходящих под фильтр, и не зависит от `size` и `lastId` |

## Пробелы покрытия

Сверка с грамматикой Fraudo, контрактами OpenAPI и изменениями XYZ-598 (2026-11-16). Приоритет — по риску регресса.

| Приоритет | Что не покрыто | Почему важно |
| --- | --- | --- |
| закрыто | Наборы данных и эмуляция — PAY-API-15, PAY-API-16 | идут через локальные агрегаторы и `TimeTemplateListener`, изменённые в XYZ-598 |
| закрыто | Перезапуск и обновление поверх данных старой версии — LC-01…LC-06 | `StartupListener` и обработчики пулов переписаны |
| закрыто | Результаты `3ds`, `acceptAndNotify`, `declineAndNotify`, `catch:` — PAY-FL-28 | исполнитель правил переписан |
| закрыто | Запрещённые конструкции в шаблоне выплаты — PO-FL-10 | новый `PayoutTemplateValidator` |
| закрыто | История платежей в правиле выплаты, группировка по `wallet_id` — PO-FL-12, PO-FL-13 | новые агрегаторы истории для выплат |
| закрыто | Функции, операторы, `isMobile`/`isRecurrent`, гео — PAY-FL-17, 18, 29 | — |
| закрыто | Окна времени — PAY-FL-30 | — |
| закрыто | `sumSuccess`/`sumError`, агрегаты по полям — PAY-FL-08, 31 | — |
| закрыто | Списки выплат по полям, `inList`, `countryBy` для выплат — PO-FL-14 | — |
| закрыто | Thrift `IsBlacklisted`, `/inspect-user` — PAY-FL-32, 25 | — |
| закрыто | Привязки по умолчанию, CSV-списки, аудит — PAY-FL-23, PAY-API-06, 10, 18, PO-API-10 | — |
| ограничение | `isRecurrent`/`isMobile`, `paymentSystem()`, `cardCategory()` через REST; `inList` для платежей | fraudbusters-api не передаёт эти данные; списки `naming` для платежей не создаются |
| закрыто | Кандидаты, аналитика — PAY-API-11, PAY-API-17 | — |
| закрыто | Нагрузка: `load` с изменением шаблона, `stress`, `spike`, `soak`, смешанная нагрузка — NF-03…NF-07, NF-09 | — |
| вне рамок | Уведомления (notificator), путь mg-connector | не затронуты / нужна заглушка fistful |

## Профили запуска

Набор и профиль выбираются независимо: `scripts/run-k6.sh test <версия> <набор> <профиль>`.

| Профиль | Набор `payments` | Набор `payouts` | Набор `mixed` |
| --- | --- | --- | --- |
| `functional` | все PAY-FL, PAY-API | все PO-FL, PO-API | все MIX |
| `smoke` | PAY-FL-01, 02, 03, 12 | PO-FL-01, 02, 05 | MIX-01 |
| `baseline`, `load`, `stress`, `spike`, `soak` | трафик NF-02…NF-07 | — | NF-09 |

Отдельные кейсы запускаются через `K6_CASES`, например `K6_CASES=PAY-FL-19,PAY-FL-20`.
Кейсы с пометкой «глобальный» в `functional` всегда выполняются последовательно после остальных.

## Вне рамок

| Что | Почему | Когда вернуться |
| --- | --- | --- |
| Путь machinegun → mg-connector → Kafka для выплат | локально `fb.stream.withdrawalEnabled=false`, нужен fistful | после заглушки fistful |
| Топик `payout_result` | в репозитории нет потребителя | когда появится потребитель |
| UI | проверяется отдельно | — |
| Notificator | не затронут XYZ-598 | — |
| Авторизация 401/403 | локально keycloak отключён | при запуске на стенде с keycloak |
| Запуск в CI | сначала локальные прогоны | после стабилизации набора |
