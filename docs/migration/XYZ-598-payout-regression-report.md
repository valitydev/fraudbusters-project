# XYZ-598: проверка старых правил и покрытия выплат в fraudbusters — 6 ноября 2026

Ветка `ft/XYZ-598/fraudo-withdrawall-impl` сравнивалась с `master`. Проверялись три вещи:
старые платёжные правила дают те же решения и работают не медленнее, сценарии выплат
полностью покрыты тестами и работают в запущенном сервисе.

## Итог

- **Решения старых платёжных правил не изменились.** На 244 560 вычислениях
  (3062 шаблона × 40 платежей × 2 режима) результат ветки совпал с `master` полностью.
- **Скорость вычисления та же, запросов к истории меньше.** Вычисление правил стоит
  ~3,5 мкс в обеих версиях. Ветка делает на 10–12% меньше запросов к истории, потому что
  одинаковые окна, записанные по-разному (`24` и `24, hours`), теперь попадают в один кэш.
- **Разбор шаблона примерно на 10% медленнее** (≈275 мкс против ≈250 мкс). Шаблоны
  разбираются только при загрузке из Kafka, а не на каждый платёж.
- **Есть три риска для уже сохранённых шаблонов и один для выкатки**
  (раздел «Что может сломаться»). Их нужно проверить до релиза.
- **Покрытие выплат дополнено.** Добавлены 19 тестов, в том числе первый сквозной тест
  выплат через Spring, Kafka, ClickHouse и thrift-эндпоинт. Полная сборка fraudbusters:
  253 теста, 0 падений, checkstyle без замечаний.

## Как проверялось

1. **Дифференциальный прогон fraudo.** Один и тот же стенд собран против jar из `master`
   (`fraudo-1.0.9`) и из ветки (`fraudo-1.1.0`). Корпус правил:
   - все `.frd` из тестов fraudo в `master`;
   - примеры из `docs/fraudo/content/docs/examples.md`;
   - 3000 случайно сгенерированных шаблонов. Они покрывают все платёжные функции, окна
     во всех формах записи, group by, `AND`/`OR`/`NOT`/скобки, сравнения строк,
     `in`/`like`/списки, `isTrusted`, все результаты и `catch`, а также повторы одного
     вызова внутри шаблона.

   Заглушки агрегаторов, списков и резолверов детерминированно зависят от аргументов,
   иногда бросают исключения и записывают каждый вызов. Сравнивались итоговые результаты
   и последовательность вызовов. Проверка шла и через first-find visitor (инспекция),
   и через full visitor (проверка на исторических данных).
2. **Разбор diff fraudbusters** на платёжном пути: агрегаторы ClickHouse, резолверы полей,
   каскад шаблонов, загрузка шаблонов, запись в ClickHouse.
3. **Тесты выплат:** по коду выписаны все пути выплат, найденные пробелы закрыты тестами.

## Старые платёжные правила

| Что | Результат |
|---|---|
| Итоговые решения на корпусе | 0 расхождений из 244 560 вычислений |
| Вызовы истории | Ветка ни разу не делает вызова, которого не было в master. Убраны только повторы уже успешного вызова с теми же аргументами (11,8% вызовов на корпусе) |
| Неудачные вызовы | Как и в master, не кэшируются и повторяются |
| Грамматика | Та же структура выражений и приоритет AND/OR/NOT. Добавлены `userId()` и функции выплат, удалена форма `isTrusted` с условиями |
| Каскад платёжных шаблонов | Эквивалентен: `createCheckedResult` никогда не возвращает чистый NOTIFY, поэтому `findFirstDecision` ведёт себя как цепочка `orElseGet` из master |
| ClickHouse-агрегаторы платежей | Эквивалентны master при `includeCurrentOperation = true`, а `PaymentFraudoConfig` передаёт `true` во всех шести местах |
| Загрузка шаблонов | Валидация и разбор только на CREATE, удаление без разбора. Как в master |

Скорость. Лучший результат из 15 прогонов, по три запуска каждой версии вперемешку:

| | master | ветка |
|---|---|---|
| Вычисление, мкс/вычисление | 3,50–3,65 | 3,48–3,65 |
| Запросов истории за прогон | 299 487 | 268 242 (−10%) |
| Разбор, мкс/шаблон | 245–251 | 270–286 (+~10%) |

В проде стоимость вычисления определяется запросами в ClickHouse, поэтому уменьшение числа
запросов важнее разницы в микросекундах.

## Что может сломаться

### 1. Шаблоны с `isTrusted(paymentsConditions(...))` / `withdrawalsConditions(...)` — проверить до релиза

Эта форма удалена из грамматики. Загрузка шаблонов ошибку не бросает: ANTLR
восстанавливается, валидатор пишет только warning, частично разобранное дерево попадает
в пул. Такое правило в ветке **тихо превращается в NOTIFY**, и проверка идёт к следующим
правилам. Проверено на ветке:

| Шаблон | master (доверенная карта) | ветка |
|---|---|---|
| `isTrusted(paymentsConditions(...)) -> accept;` | ACCEPT | NOTIFY |
| то же `AND 1 > 0 -> accept;` + `1 > 0 -> decline;` | ACCEPT | NOTIFY, затем DECLINE |

Проверка в базе management:

```sql
select id from af.f_template where template ~ '(payments|withdrawals)Conditions';
```

Найденные шаблоны нужно перевести на `isTrusted("<имя шаблона условий>")` до выкатки.

### 2. Колонки ClickHouse — до выкатки

Запись платежей, возвратов, чарджбэков, `events_unique` и `fraud_payment` теперь передаёт
`customerId` и `userId`, а запись выплат — новые поля идентичности. fraudbusters не
применяет миграции ClickHouse сам. Без
`apps/fraudbusters/src/test/resources/sql/V12__add_client_identity.sql` и `V13__add_payout_identity.sql`
на продовом ClickHouse **вставки упадут, и платёжная история перестанет пополняться**.

Отдельно: `V13__add_payout_identity.sql` лежит ещё и в
`apps/fraudbusters/src/main/resources/db/migration/`, хотя оттуда его никто не читает.
Для V12 в коммите `7dff1064` решили держать скрипты только рядом с остальными
ClickHouse-скриптами. Копию V13 в `main` стоит убрать, чтобы она не вводила в заблуждение.

### 3. Имена правил, совпадающие с новыми ключевыми словами

`rule:userId: ...`, `rule:countPayouts: ...`, `rule:sumPayoutsError: ...`,
`rule:uniquePayouts: ...` и подобные в master разбираются, а в ветке нет. В проде такое
правило тоже тихо станет NOTIFY. Это маловероятно, но легко проверить:

```sql
select id from af.f_template
where template ~ 'rule:\s*(userId|countPayouts\w*|sumPayouts\w*|uniquePayouts)\s*:';
```

Если найдутся, проще всего переименовать правило. Альтернатива — разрешить ключевые слова
в имени правила в грамматике, вместе с `RuleKeyGenerator`.

### 4. Правила, которые в master падали, теперь вычисляются

Чтобы платёжные правила могли читать историю выплат, резолверы полей платежа научились
отдавать `party_id`, `shop_id` и `currency`, а `unique` — считать по `currency` и
`country_bank`. В master такие правила всегда падали и давали результат `catch` или
NOTIFY, теперь они считаются по-настоящему. Затронутые формы: `unique(.., "currency"|"country_bank", ..)`,
а также `in`/`like`/`countryBy`/`inWhiteList`/`inBlackList`/`inGreyList`/`inList`
по `party_id`, `shop_id`, `currency`. Скорее это исправление, но решения таких шаблонов
поменяются:

```sql
select id from af.f_template
where template ~ 'unique\("[a-z_]+",\s*"(currency|country_bank)"'
   or template ~ '(in|like|countryBy|inWhiteList|inBlackList|inGreyList|inList)\([^)]*"(party_id|shop_id|currency)"';
```

### 5. Мелкие намеренные изменения

- Локальное хранилище (`LocalResultStorageRepository`) в `unique` больше не считает пустое
  значение отдельным уникальным. Так же, как ClickHouse не считает NULL.
- Повторный id шаблона в группе применяется один раз, с высшим приоритетом. Если такой
  шаблон давал notify, уведомление больше не дублируется.
- Ключ кэша `inspectUser` включает `userId`. Кэш в памяти (Caffeine), поэтому после
  рестарта это ни на что не влияет.

## Покрытие сценариев выплат

✅ — покрыто, 🆕 — добавлено в этой проверке.

| Сценарий | Тесты |
|---|---|
| **Сквозной путь в запущенном сервисе** | |
| Шаблоны и ссылки выплат приходят из своих Kafka-топиков и сразу применяются | 🆕 `PayoutEndToEndIntegrationTest` |
| `inspectWithdrawal` через thrift `/fraud_inspector/v1`: без шаблонов high, accept → low, highRisk → high, decline → fatal | 🆕 `PayoutEndToEndIntegrationTest` |
| Правило выплат над настоящей историей ClickHouse (`countPayoutsSuccess` с группировкой по party) | 🆕 `PayoutEndToEndIntegrationTest` |
| Платёжное правило над историей выплат (`countPayouts` в шаблоне платежа) | 🆕 `PayoutEndToEndIntegrationTest`, 🆕 `PayoutHistoryRepositoryTest.paymentRulesReadPayoutHistoryWithPaymentValues` |
| Шаблоны платежа и выплаты с одним id не мешают друг другу | 🆕 `PayoutEndToEndIntegrationTest`, ✅ `PayoutInspectionTest.payoutCommandsDoNotChangePaymentTemplates…` |
| Невалидный запрос (пустой кошелёк) → `InvalidRequest` через thrift | 🆕 `PayoutEndToEndIntegrationTest` |
| **Каскад шаблонов выплат** | |
| Порядок: глобальный шаблон → глобальная группа → группа party → группа кошелька → шаблон party → шаблон кошелька, по умолчанию HIGH_RISK | 🆕 `PayoutTemplateVisitorImplTest.everyLevelDecidesInCascadeOrder` |
| Следующие уровни не вычисляются после решения | 🆕 `PayoutTemplateVisitorImplTest.laterLevelsAreNotEvaluatedOnceALevelDecides` |
| Другой кошелёк той же party получает только уровни party и глобальные | 🆕 `PayoutTemplateVisitorImplTest`, 🆕 `PayoutEndToEndIntegrationTest` |
| party и кошелёк обязательны | 🆕 `PayoutTemplateVisitorImplTest.partyAndWalletAreRequired` |
| Приоритеты внутри группы, глобальный шаблон важнее группы | ✅ `PayoutInspectionTest.groupsAreOrderedAndGlobalRulesTakePrecedence` |
| Уведомления идут дальше, одиночный notify → low, уведомления группы сохраняются с решением | ✅ `PayoutInspectionTest.notifications…`, `groupNotifications…` |
| **Загрузка шаблонов** | |
| Тип операции определяется топиком | ✅ `PayoutInspectionTest.topicDefinesTheOperationType` |
| Удаление шаблона, кошелёк без party отклоняется | ✅ `PayoutInspectionTest.walletRuleCanQueryPayoutHistory`, `payoutCommandsDoNotChange…` |
| Поля без данных для выплат отклоняются при загрузке | ✅ `PayoutInspectionTest.fieldsWithoutDataForPayoutsAreRejected` (6 случаев), ✅ `PayoutTemplateValidatorTest` |
| Повторный шаблон в группе | ✅ `GroupListenerTest.listenKeepsRepeatedTemplateOnceAtHighestPriority` |
| **Обработчик** | |
| Результат публикуется в свой топик `payout_result`, платёжный не трогается | ✅ `PayoutInspectionTest.damselMethodPublishesPayoutResultsToTheirOwnTopic` |
| Пустые party/кошелёк → `InvalidRequest`, сбой → `TException`, в обоих случаях без публикации | 🆕 `FraudInspectorHandlerWithdrawalTest` |
| Контекст без обязательных полей отклоняется до проверки | 🆕 `FraudInspectorHandlerWithdrawalTest` |
| **Модель выплаты** | |
| Перенос всех полей: карта, страна, платёжная система, IP, fingerprint, email, телефон, userId/customerId, время | 🆕 `WithdrawalContextToModelConverterTest` |
| Без `createdAt` берётся текущее время, необязательные части остаются пустыми | 🆕 `WithdrawalContextToModelConverterTest` |
| customerId не подставляется как userId | ✅ `PayoutInspectionTest.noDecisionIsHighAndCustomerIsNotUsedAsUser`, ✅ `UserIdentityAggregationTest` |
| **История выплат (настоящий ClickHouse)** | |
| Повторная загрузка, обогащение и смена статуса считаются один раз | ✅ `PayoutHistoryRepositoryTest.replaysEnrichmentAndStatusChangesAreCountedOnce` |
| Все агрегаты: count/success/error ± код, sum/success/error ± код, unique, группировки | ✅ `PayoutHistoryRepositoryTest` |
| Пустые значения не совпадают со старыми строками, текущая выплата не считается | ✅ `PayoutHistoryRepositoryTest.missingValues…` |
| Поля, которых нет в таблице, отклоняются | ✅ `PayoutHistoryRepositoryTest.rejectsFields…`, ✅ `HistoryFieldResolverImplTest` |
| Запись старых и обогащённых выплат | ✅ `PayoutHistoryRepositoryTest.legacyAndEnriched…` |
| Границы окна: старше окна, позже проверяемой выплаты, окно `48, 24` | 🆕 `PayoutHistoryRepositoryTest.onlyOperationsInsideTheWindowAreCounted` |
| История платежей для правил выплат не учитывает саму выплату | ✅ `UserIdentityAggregationTest.inspectingPayoutDoesNotAddItToPaymentHistory` |
| **Списки** | |
| Именованные списки с идентичностью выплаты | ✅ `PayoutInListFinderImplTest.namedLists…` |
| Серый список: лимит, party и кошелёк в подсчёте, период (не начался, истёк, пустой), отсутствие строки, без начала периода | 🆕 + ✅ `PayoutInListFinderImplTest` |
| Пустые значения не запрашиваются, чёрный/белый список по своему типу, сбой сервиса → ошибка правила | 🆕 `PayoutInListFinderImplTest` |
| **Язык (fraudo)** | |
| Все функции выплат, коды ошибок, группировки, неизвестные поля | ✅ `CountPayoutTest`, `SumPayoutTest`, `UniquePayoutTest`, `PayoutFunctionsTest` |
| Кэш: функции, аргументы, окна, пробелы, потоки, очистка между проверками | ✅ `PayoutCacheTest`, `PaymentCacheTest`, ✅ `PayoutInspectionTest.mixedHistoryCaches…` |
| Ошибки истории, `catch`, повтор упавшего вызова | ✅ `PayoutFailureTest` |
| Валидатор шаблонов выплат | ✅ `PayoutTemplateValidatorTest` |
| userId в обоих visitor | ✅ `ClientIdentityTest` |

## Что не покрыто

- **Предзагрузка выплатных топиков при старте** (`StartupListener`). Сквозной тест
  публикует шаблоны после старта контекста, поэтому проверен путь живого слушателя,
  а чтение накопленных топиков при рестарте — нет.
- **Стоимость запроса истории выплат на объёмах прода.** `PayoutHistoryQuery` делает
  `argMax … group by id` по окну. На тестовых данных это быстро, но на окне с большим числом
  выплат стоит посмотреть план и время запроса на копии продовых данных.
- **Другие сервисы** (management, wb-list-manager, mg-connector) в эту проверку не входили.

## Как повторить

```bash
cd apps/fraudbusters && mvn -o verify
```

Дифференциальный стенд fraudo (генератор корпуса, общий прогон и адаптеры под обе версии)
собран вне репозитория. Его можно добавить в `scripts/`, если нужно повторять сравнение
перед следующими изменениями языка.
