# Выплаты

Набор `payouts`: проверка выплаты, история, каскад шаблонов и групп, списки и API management для выплат.
Нужна функциональность `payouts`: на версиях без неё все кейсы набора `skipped`.

Каскад шаблонов выплаты: глобальный шаблон → глобальная группа → группа party → группа party+wallet →
шаблон party → шаблон party+wallet. Для проверки выплаты обязательны `party_id` и `wallet_id`.

Профили: F — `functional`, S — `smoke`, L — входит в нагрузочный трафик набора.

| ID | Проверка | Профили |
| --- | --- | --- |
| PO-FL-01 | Выплата без риска: проверка, история | F S L |
| PO-FL-02 | Нет привязки и неполный контекст | F S |
| PO-FL-03 | Каскад шаблонов и групп выплат | F |
| PO-FL-04 | Глобальная привязка выплат (глобальный) | F |
| PO-FL-05 | Списки выплат и их область действия | F S L |
| PO-FL-06 | `countPayouts`, `sumPayouts` | F L |
| PO-FL-07 | `countPayoutsSuccess`, `sumPayoutsSuccess` | F |
| PO-FL-08 | `countPayoutsError`, `sumPayoutsError` с кодом ошибки | F |
| PO-FL-09 | `uniquePayouts` | F |
| PO-FL-10 | Запрещённые конструкции в шаблоне выплаты | F |
| PO-FL-11 | Изменение и удаление шаблона выплаты | F |
| PO-FL-12 | Функции истории платежей в правиле выплаты | F |
| PO-FL-13 | Группировка истории выплат по `wallet_id` | F |
| PO-FL-14 | Списки выплат по полям, именованный список, countryBy | F |
| PO-API-01 | Валидация шаблона выплаты | F |
| PO-API-02 | Создание, обновление, удаление шаблона выплаты | F |
| PO-API-03 | Поиск шаблонов выплат и пагинация | F |
| PO-API-04 | Имена шаблонов выплат | F |
| PO-API-05 | Привязки шаблонов выплат | F |
| PO-API-06 | Группы шаблонов выплат | F |
| PO-API-07 | Привязки групп выплат | F |
| PO-API-08 | Списки выплат | F |
| PO-API-09 | История выплат: фильтры, последнее состояние, пагинация | F |
| PO-API-10 | Аудит команд выплат | F |
| PO-API-11 | Некорректные параметры API выплат | F |
| PO-API-12 | Загрузка выплат в fraudbusters-api | F |

## Сквозные сценарии (PO-FL)

Проверка выплаты — Thrift `InspectorProxySrv.inspectWithdrawal(WithdrawalContext)`.
Загрузка истории — `POST /payouts` в fraudbusters-api со статусами `pending`, `succeeded`, `failed`.
Шаблон привязывается к party+wallet кейса, если не сказано иное.

### PO-FL-01. Выплата без риска: проверка, история
- **Предусловия:** шаблон выплаты `rule: amount() >= 100000 -> decline; rule: amount() < 100000 -> accept;`.
- **Шаги:** проверить выплату на 5000; загрузить её со статусом `succeeded`; найти в
  `GET /payouts-historical-data/payouts` по `payoutId`; проверить выплату на 150000.
- **Ожидание:** `low`; запись найдена с party, wallet, суммой и статусом `succeeded`; вторая проверка — `fatal`.

### PO-FL-02. Нет привязки и неполный контекст
- **Шаги:** проверить выплату party+wallet без привязок; проверить выплату без `wallet_id`; без `party_id`.
- **Ожидание:** без привязок — `high`; пустые `party_id`/`wallet_id` — исключение `InvalidRequest`
  с текстом `party_id and wallet_id are required for payout inspection`. Отсутствующее поле Thrift отклоняется
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

## API управления и загрузки (PO-API)

### PO-API-01. Валидация шаблона выплаты
- **Шаги:** `POST /payouts-templates/validate` с корректным шаблоном, с ошибкой синтаксиса,
  с функциями истории выплат, с кодом ошибки `k6: limit` в `countPayoutsError`.
- **Ожидание:** корректный шаблон и шаблон с функциями выплат — без ошибок; ошибочный — ошибка с текстом;
  код с пробелом у `:` — ошибка с этим кодом.

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
  1. `GET /payouts-historical-data/payouts` с каждым фильтром.
  2. Загрузить выплату со статусом `pending`, затем ту же со `succeeded`; искать по `payoutId`,
     по `payoutStatus=pending` и `succeeded`.
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
- **Ожидание:** 4xx, ни одного 5xx. Коды ответов — наблюдения.

### PO-API-12. Загрузка выплат в fraudbusters-api
- **Шаги:** `POST /payouts` с пустым списком, с записью без обязательных полей, с `walletId` без `partyId`.
- **Ожидание:** пустой список — 201; без обязательных полей — 400; wallet без party — 400.
