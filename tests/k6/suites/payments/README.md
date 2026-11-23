# Платежи

Набор `payments`: проверка платежа, история, каскад шаблонов, списки и API management для платежей.
Работает на любой версии Fraudbusters. Кейс PAY-FL-16 проверяет синтаксис, снятый в `payouts`, и выполняется только
на версиях без этой функциональности.

Каскад шаблонов платежа: глобальный шаблон → группа party → группа party+shop → шаблон party → шаблон party+shop.
Применяется первое терминальное решение; `notify` каскад не останавливает.

Профили: F — `functional`, S — `smoke`, L — входит в нагрузочный трафик набора.

| ID | Проверка | Профили |
| --- | --- | --- |
| PAY-FL-01 | Платёж без риска: проверка, история, результат проверки | F S L |
| PAY-FL-02 | Отклонение по сумме | F S L |
| PAY-FL-03 | Нет привязки или ни одно правило не сработало | F S |
| PAY-FL-04 | `count` по card_token в рамках party и shop | F L |
| PAY-FL-05 | `sum` по fingerprint с условием на валюту | F |
| PAY-FL-06 | `unique` email по ip | F |
| PAY-FL-07 | `countSuccess`, `countError`, `countPending` | F |
| PAY-FL-08 | `sumSuccess`, `sumError` | F |
| PAY-FL-09 | `countRefund`, `sumRefund` | F |
| PAY-FL-10 | `countChargeback`, `sumChargeback` | F |
| PAY-FL-11 | Идентичность клиента: `count("user_id", …, "party_id")` | F L |
| PAY-FL-12 | Чёрный список | F S L |
| PAY-FL-13 | Белый список | F |
| PAY-FL-14 | Серый список с лимитом | F |
| PAY-FL-15 | Именованный список `inList` (не выполняется) | — |
| PAY-FL-16 | Доверенная карта `isTrusted(paymentsConditions(...))`, до `payouts` | F |
| PAY-FL-17 | Гео: `countryBy("ip")` | F |
| PAY-FL-18 | `isRecurrent`, `isMobile` | F |
| PAY-FL-19 | Каскад: шаблон party и party+shop | F |
| PAY-FL-20 | Каскад: группа и шаблон, приоритеты в группе | F |
| PAY-FL-21 | Каскад: глобальный шаблон (не выполняется) | — |
| PAY-FL-22 | Каскад: `notify` не останавливает проверку | F |
| PAY-FL-23 | Привязка по умолчанию для нового магазина | F |
| PAY-FL-24 | Изменение и удаление шаблона, удаление привязки | F |
| PAY-FL-25 | Проверка пользователя `/inspect-user` | F |
| PAY-FL-26 | Загрузка fraud-платежей | F |
| PAY-FL-27 | Шаблон в форме боевого: списки, like, доверие, pending, коды ошибок | F |
| PAY-FL-28 | Результаты правил: 3ds, *AndNotify, уведомления, catch | F |
| PAY-FL-29 | Функции и операторы: userId, paymentSystem, cardCategory, rand, like, not, or, !=, дроби | F |
| PAY-FL-30 | Окна времени: minutes, calendar_days, calendar_months, смещение, без единицы | F |
| PAY-FL-31 | Агрегаты по полям: email, ip, fingerprint, phone, bin, country_bank | F |
| PAY-FL-32 | Thrift IsBlacklisted | F |
| PAY-API-01 | Валидация шаблона | F |
| PAY-API-02 | Создание, обновление, удаление шаблона | F |
| PAY-API-03 | Поиск шаблонов и пагинация | F |
| PAY-API-04 | Имена шаблонов | F |
| PAY-API-05 | Привязки шаблонов | F |
| PAY-API-06 | Привязки по умолчанию | F |
| PAY-API-07 | Группы шаблонов | F |
| PAY-API-08 | Привязки групп | F |
| PAY-API-09 | Списки: строки, имена, поиск | F |
| PAY-API-10 | Списки: загрузка и удаление CSV | F |
| PAY-API-11 | Списки: кандидаты | F |
| PAY-API-12 | История платежей: фильтры и пагинация | F |
| PAY-API-13 | История возвратов, чарджбэков, fraud-платежей | F |
| PAY-API-14 | История результатов проверки | F |
| PAY-API-15 | Наборы данных и применение правила к истории | F |
| PAY-API-16 | Эмуляция каскада шаблонов | F |
| PAY-API-17 | Аналитика | F |
| PAY-API-18 | Аудит | F |
| PAY-API-19 | Некорректные параметры management | F |
| PAY-API-20 | Загрузка данных в fraudbusters-api | F |

## Сквозные сценарии (PAY-FL)

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
- **Ожидание:** party A — `fatal`; party B — `low`; `user_id` без `customerId` — 400.

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
- **Статус:** не выполняется: `POST /payments-lists` (и CSV) не принимает тип `naming` (500 `UnknownEventException`). `inList` проверяется для выплат в PO-FL-14.

### PAY-FL-16. Доверенная карта
- **Доступность:** до `payouts`: функциональность убрала из грамматики формы `isTrusted(paymentsConditions(...))` и
  `isTrusted(payoutsConditions(...))`.
- **Предусловия:** шаблон `rule: isTrusted(paymentsConditions(condition("RUB", 1, 1, 1))) -> accept;`.
- **Шаги:** проверить платёж; загрузить успешные платежи по card_token, достаточные для условия; проверить снова (с ожиданием).
- **Ожидание:** `high` → `low`.

### PAY-FL-17. Гео
- **Шаги:** шаблон с правилами `countryBy("country_bank") = "RUS"/"RU"`, `in(countryBy("ip"), …)`,
  `countryBy("ip") = "UNKNOWN"`; платежи с частным ip `10.0.0.1`, с публичным `1.208.61.253`, с картой `RUS`.
- **Ожидание:** частный ip — `accept/ip_unknown`. Наблюдения: решение по стране банка (код
  передаётся как alpha-3) и по публичному ip.

### PAY-FL-18. `isRecurrent`, `isMobile`
- **Шаги:** шаблон `recurrent: isRecurrent() -> decline; mobile: isMobile() -> highRisk; other -> accept`; платежи с
  `payerType` `recurrent` и `mobile`, с `tokenMobileProvider`, обычный.
- **Ожидание:** обычный — `accept/other`. Наблюдения: решения для `recurrent`, `mobile` и с провайдером токена.
  Ограничение: fraudbusters-api всегда передаёт `Payer.payment_resource` и не передаёт провайдера токена, а fraudbusters берёт оба
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
     который применяется первым.
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
  с NPE и становится уведомлением. fraudbusters-api нужен `fraudbusters.service.user.inspector.url`.

### PAY-FL-26. Загрузка fraud-платежей
- **Статус:** покрыт PAY-API-13 (загрузка fraud-платежей через CSV и fraudbusters-api, поиск в истории).

### PAY-FL-27. Шаблон в форме боевого
Шаблон повторяет конструкции рабочего платёжного шаблона с другими именами и значениями
(данные: [PAY-FL-27](../../data/payments/PAY-FL-27.js)): белый и чёрный списки по `email`,
`fingerprint`, `ip`; `like` с экранированной точкой; правила с одинаковыми именами и два правила в одной строке;
`and`/`or`/`not` в нижнем регистре и вложенные скобки; `trust` по `countSuccess` + `countError` с кодом ошибки и по
`sumSuccess`; `accept` по месячной сумме; `decline` по `countPending` в минутах и по `countError` за сутки;
исключение `not in("email", "UNKNOWN")`; код ошибки `card_expired:k6` в нескольких правилах.
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

- **Наблюдения:** ответ `/inspect-payment` на `trust` (в REST-контракте нет `trusted`);
  решение для email, переданного как `UNKNOWN` (fraudbusters приводит email к нижнему регистру).

### PAY-FL-28. Результаты правил: 3ds, *AndNotify, уведомления, catch
- **Шаги:** шесть party+shop с шаблонами: `-> 3ds`; `-> acceptAndNotify`; `-> declineAndNotify`; два `notify` и затем
  `decline`; `rand(0) > 1 -> decline catch: highRisk` и запасное `accept`; то же без `catch`. Проверить шаблон с
  `catch: decline`.
- **Ожидание** (результат / правило / ответ REST): `three_ds/ask_3ds/high`; `accept_and_notify/accept_notify/low`;
  `decline_and_notify/decline_notify/fatal`; `decline/final_decline/fatal`; `high_risk/broken/high` (ошибка
  `rand(0)` уходит в `catch`); без `catch` ошибочное правило становится уведомлением и решает `accept/fallback/low`.
  `catch: decline` отклоняется валидацией: грамматика допускает в `catch` только `trust`, `accept`, `3ds`,
  `highRisk`, `notify` (пример `catch: decline` в документации Fraudo неверен). Наблюдение: `notificationsRule`
  в результатах проверки.

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
- **Шаги:** `count("<поле>", 1, days, "party_id", "shop_id") > 2` для email, ip, fingerprint, phone, bin,
  country_bank; по 3 платежа с одинаковым значением поля.
- **Ожидание:** email, ip, fingerprint, phone, bin — `fatal`. Наблюдение: решение по `country_bank`. Шаблон с
  `masked_pan` в функции истории отклоняется валидацией (PAY-API-01).

### PAY-FL-32. Thrift IsBlacklisted
- **Шаги:** строка чёрного списка `CARD_TOKEN` для party+shop; `InspectorProxySrv.IsBlacklisted` для этой карты,
  другой карты и другого магазина.
- **Ожидание:** `true`, `false`, `false`.

## API управления и загрузки (PAY-API)

Пути указаны относительно `/fb-management/v1`. Пагинация проверяется по инвариантам [PG](../README.md#пагинация-pg).

### PAY-API-01. Валидация шаблона
- **Шаги:** `POST /payments-templates/validate` с корректным шаблоном, с синтаксической ошибкой, с кодом ошибки
  `card_expired: k6` в `countError` и с `count("masked_pan", ...)`.
- **Ожидание:** для корректного — список ошибок пуст; для синтаксической ошибки — ошибка с непустым текстом;
  код с пробелом у `:` и поле, которого нет в истории платежей, — ошибка с этим кодом или полем.

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
  Наблюдение: ответ на привязку к несуществующему шаблону.
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
- **Ожидание:** найдено 5 строк, после удаления — 0; CSV серого списка с ISO-временем принят, строка в сером списке.

### PAY-API-11. Списки: кандидаты
- **Шаги:** `POST /payments-lists/candidates` с чарджбэком (email, ip, fingerprint клиента); найти кандидатов по
  значению (`GET /payments-lists/candidates`, поиск по значению, имени списка или batch id — точное совпадение);
  `GET /payments-lists/candidates-batches`; одобрить кандидатов email и fingerprint
  (`POST /payments-lists/candidates/approved`).
- **Ожидание:** три кандидата чёрного списка с party и shop чарджбэка; одобренный email появляется в чёрном списке
  `EMAIL`, неодобренный ip — нет. Кандидаты уникальны по значению: в каждом прогоне свой ip.
  Кандидат по fingerprint — в списке `FINGERPRINT`, после одобрения строка появляется в нём.

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
  время `yyyy-MM-dd HH:mm:ss`), третий — через `POST /fraud-payments` fraudbusters-api с ISO-временем.
- **Ожидание:** возвраты и чарджбэки найдены по party; все три fraud-платежа найдены по `paymentId` с типом и
  комментарием из загрузки.

### PAY-API-14. История результатов проверки
- **Шаги:** выполнить проверки из PAY-FL-01 и PAY-FL-02; искать в `/payments-historical-data/inspect-results` по `paymentId`, `invoiceId`, `template`.
- **Ожидание:** найдены результаты с решением, шаблоном и правилом; фильтр `template` отсекает чужие проверки.
  Платёж из fraudbusters-api сохраняется в результатах как `<id>.<id>` (invoiceId.paymentId), фильтр `paymentId`
  ищет это составное значение; фильтр `invoiceId` находит одну проверку. Наблюдение: фильтр `paymentId=<id>`.

### PAY-API-15. Наборы данных и применение правила к истории
- **Шаги:** создать набор данных из 4 платежей одной карты (5000, 5000, 150000, 5000) (`/payments-data-set/data-sets`);
  получить его и найти по имени с окном `from`/`to`; применить к строкам набора шаблон
  `big_amount: amount() >= 100000 -> decline; card_velocity: count("card_token", 1, days) > 2 -> decline; other -> accept`
  (`applyRuleOnHistoricalDataSet`, записи — строки сохранённого набора с их числовыми id); получить проверенный
  набор по id, который вернул `applyRuleOnHistoricalDataSet` (`/payments-data-set/checked-data-sets/{id}`); удалить набор.
- **Ожидание:** `applyRuleOnHistoricalDataSet` возвращает id набора данных; 4 проверенные строки; платёж 150000 — `decline/big_amount`, остальные — `accept/other` или
  `decline/card_velocity`. Наблюдения: решения по строкам, фильтр без `from`/`to`, получение удалённого
  набора.

### PAY-API-16. Эмуляция каскада
- **Шаги:** настроить группу party и шаблон party+shop; запросить `GET /payments-emulations` для party+shop.
- **Ожидание:** возвращены все три шаблона, шаблон party раньше шаблона party+shop. Наблюдение: порядок шаблонов.

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
  Наблюдение: справочник типов объектов.

### PAY-API-19. Некорректные параметры management
- **Шаги:** запросы без обязательных `from`/`to`; `sortOrder=FOO`; неизвестный `listType`; `size=0`, `size=-1`, без `size`.
- **Ожидание:** ни одного 5xx. Коды ответов — наблюдения.

### PAY-API-20. Загрузка данных в fraudbusters-api
- **Шаги:** `POST /payments`, `/refunds`, `/chargebacks`, `/fraud-payments` с пустым списком и с записью без обязательных полей.
- **Ожидание:** пустой список — 201; запись без обязательных полей — 400.
