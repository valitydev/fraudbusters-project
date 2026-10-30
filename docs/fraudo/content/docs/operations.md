---
title: operations

metatitle: operations

metadescription: operations

category: operations
---

# Функции языка

## Структура грамматик

`dev.vality.fraudo/FraudoRules.g4` собирает язык из модулей и задаёт доступные виды выражений:

- `FraudoCommon.g4` — общий синтаксис, логические операции, поля, списки, окна времени и токены;
- `FraudoPayment.g4` — функции истории платежей и платёжные проверки;
- `FraudoPayout.g4` — функции истории выплат.

Для нового набора функций добавляется модуль, его импорт и ссылки в соответствующих
выражениях сборки. Основная грамматика генерирует `FraudoRulesParser`,
`FraudoRulesLexer` и `FraudoRulesBaseVisitor`. При обновлении Fraudo сервисам нужно
заменить ссылки на прежние классы `FraudoPayment*` новыми именами `FraudoRules*`.

Общие посетители `visitor.FirstFindRulesVisitor` и `visitor.FullRulesVisitor`
исполняют функции платежей и выплат. Первый останавливается на первом решении,
второй обходит все правила. `payout.visitor.PayoutFuncVisitor` отвечает только
за функции истории выплат. Прежний `payment.visitor.impl.FullVisitorImpl` заменён
на `visitor.FullRulesVisitor`. Посетители принимают готовый `PaymentFuncVisitor`,
который фабрика создаёт со всеми платёжными и trust-зависимостями.

Для обоих типов операций используются общие реализации
`resolver.impl.GroupFieldsResolverImpl` и `resolver.impl.TimeWindowResolverImpl`.
В `ResolverBundle` резолвер группировок доступен через `getGroupFieldsResolver()`.

## Параметры функций

#### Поля группировки (group_field):

Список полей которые можно использовать для группировок при подсчете сумм, количества и уникальности

    email
    ip
    fingerprint
    bin
    shop_ip
    party_id
    card_token

#### Единицы времени (time_unit):

Минуты, часы и дни работают как разница, то есть сейчас **_13:54_**

=> **_1 hours_** означает, что функция будет считать от **_12:54_**

В свою очередь calendar_months и calendar_days означают календарное значение, то есть **_13:54 05.04.2022_**

=> **_1 calendar_days_** означает, что функция будет считать от **_00:00 05.04.2022_**

    minutes
    hours
    days
    calendar_months
    calendar_days

## Подсчет количества

Подсчет количества платежей в разрезе поля указанного в ```group_field``` за период по num_time, time_unit, также могут
быть указаны дополнительные поля группировки (party_id, shop_id и т.д.)

```sh
count("group_field", num_time, time_unit, ["group_by_additional_fields"])
```

Подсчет количества успешных платежей в разрезе поля указанного в ```group_field``` за период по num_time, time_unit,
также могут быть указаны дополнительные поля группировки (party_id, shop_id и т.д.)

```sh
countSuccess("group_field", num_time, time_unit, ["group_by_additional_fields"])
```

Подсчет количества ошибочных платежей в разрезе поля указанного в ```group_field``` за период
по ```num_time, time_unit```, с указанием конкретного кода ошибки ```error_code```
также могут быть указаны дополнительные поля группировки (```party_id, shop_id``` и т.д.)

```sh
countError("group_field", num_time, time_unit, "error_code", ["group_by_additional_fields"])
```

Подсчет количества чарджбеков в разрезе поля указанного в ```group_field``` за период по num_time, time_unit, также
могут быть указаны дополнительные поля группировки (```party_id, shop_id``` и т.д.)

```sh
countChargeback("group_field", num_time, time_unit, ["group_by_additional_fields"])
```

Подсчет количества возвратов в разрезе поля указанного в ```group_field``` за период по num_time, time_unit, также могут
быть указаны дополнительные поля группировки (```party_id, shop_id``` и т.д.)

```sh
countRefund("group_field", num_time, time_unit, ["group_by_additional_fields"])
```

[Примеры](../examples/#_5)

## Подсчет сумм

Подсчет суммы платежей в разрезе поля указанного в ```group_field``` за период по num_time, time_unit, также могут быть
указаны дополнительные поля группировки (```party_id, shop_id``` и т.д.)

```sh
sum("group_field", num_time, time_unit, ["group_by_additional_fields"])
```

Подсчет суммы успешных платежей в разрезе поля указанного в ```group_field``` за период по num_time, time_unit, также
могут быть указаны дополнительные поля группировки (```party_id, shop_id``` и т.д.)

```sh
sumSuccess("group_field", num_time, time_unit, ["group_by_additional_fields"])
```

Подсчет суммы ошибочных платежей в разрезе поля указанного в ```group_field``` за период по ```num_time, time_unit```, с
указанием конкретного кода ошибки ```error_code```
также могут быть указаны дополнительные поля группировки (```party_id, shop_id``` и т.д.)

```sh
sumError(("group_field", num_time, time_unit, "error_code", ["group_by_additional_fields"])
```

Подсчет суммы чарджбеков в разрезе поля указанного в ```group_field``` за период по num_time, time_unit, также могут
быть указаны дополнительные поля группировки (```party_id, shop_id``` и т.д.)

```sh
sumChargeback("group_field", num_time, time_unit, ["group_by_additional_fields"])
```

Подсчет суммы возвратов в разрезе поля указанного в ```group_field``` за период по num_time, time_unit, также могут быть
указаны дополнительные поля группировки (```party_id, shop_id``` и т.д.)

```sh
sumRefund("group_field", num_time, time_unit, ["group_by_additional_fields"])
```

[Примеры](../examples/#_5)

## Количество уникальных значений

Подсчет количества уникальных значений поля by_field в разрезе поля указанного в ```group_field``` за период по
num_time, time_unit, также могут быть указаны дополнительные поля группировки (```party_id, shop_id``` и т.д.)

```sh
unique(("group_field", "by_field", num_time, time_unit, ["group_by_additional_fields"])
```

[Примеры](../examples/#ip-email)

## Проверка по спискам

Производится проверка в предзагруженных списках (белом, черном, именованном, серым)

```sh
inWhiteList("field")
inBlackList("field")
inList("test", "email")
inGreyList("email")
```

[Примеры](../examples/#_3)

## Определение типа платежа

Определение, что выставлен признак мобильного платежа

```sh
isMobile()
```

[Примеры](../examples/#_12)

Определение, что выставлен признак рекурентного платежа

```sh
isRecurrent()
```

[Примеры](../examples/#_11)

## Определение доверенного клиента

С помощью данной функции можно определелить доверяем ли мы данному плательщику

```sh
isTrusted()
```

[Примеры](../examples/#_8)

## Вспомогательные функции

Сравнивает поля с конкретными значениями в списке

```sh
in(("field", "first", "second", ...)
```

[Примеры](../examples/#_13)

Сравнивает соотвествия поля регулярному выражению

```sh
like("field", "regexp_in_java_style"[1])
```

[Примеры](../examples/#_13)

Возвращает сумму текущего платежа

```sh
amount()
```

[Примеры](../examples/#_6)

Возвращает страну по предоставленному IP

```sh
country() - this function can return result "unknown", you must remember it!
```

[Примеры](../examples/#ip)

Возвращает тип карты debit/credit

```sh
cardCategory()
```

[Примеры](../examples/#_9)

Возвращает платежную систему VISA/MASTERCARD и т.д.

```sh
paymentSystem()
```

[Примеры](../examples/#_10)

## Возможные результаты применения шаблона:

    accept 
    3ds
    highRisk
    decline
    normal
    notify
    declineAndNotify
    acceptAndNotify

## Возможные результаты обработки исключений (catch):

    accept
    3ds
    highRisk
    notify

### Идентификация клиента

`userId()` возвращает `user_id` в системе мерчанта. Регистр идентификатора сохраняется.
Для старых запросов без этих данных функции возвращают `UNKNOWN`.

В операциях `count`, `sum`, `unique`, `like` и списках доступны имена полей
`user_id`. `customer_id` передаётся в протоколах и хранится во внутреннем контексте,
но отдельной функции или поля для него в языке нет.

Все операции правил используют только переданный `user_id`, независимо от
группировки по `party_id` или `shop_id`. Если `user_id` отсутствует, `customer_id`
не подставляется вместо него. `customer_id` сохраняется как информация о клиенте
и не используется в правилах.

```text
rule: count("user_id", 60, "party_id") > 5 -> decline;
rule: count("user_id", 60, "shop_id") > 5 -> decline;
rule: count("user_id", 60) > 5 -> decline;
rule: userId() != "UNKNOWN" -> notify;
```

Один `user_id` может обозначать разных пользователей у разных party.

## История выплат

Функции истории выплат доступны в общей грамматике правил платежей и выплат:

| Функция | История |
|---|---|
| `countPayouts`, `sumPayouts` | Завершённые выплаты: `succeeded` и `failed` |
| `countPayoutsSuccess`, `sumPayoutsSuccess` | Только `succeeded` |
| `countPayoutsError`, `sumPayoutsError` | Только `failed`, с необязательным кодом ошибки |
| `uniquePayouts` | Уникальные значения атрибута завершённых выплат |

Аргументы времени и группировки соответствуют платёжным функциям:

```text
countPayouts("group_field", num_time, time_unit, ["group_by_fields"])
sumPayouts("group_field", num_time, time_unit, ["group_by_fields"])
uniquePayouts("group_field", "by_field", num_time, time_unit, ["group_by_fields"])
countPayoutsError("group_field", num_time, time_unit, "error_code", ["group_by_fields"])
sumPayoutsError("group_field", num_time, time_unit, "error_code", ["group_by_fields"])
```

Как и у `countError`/`sumError`, первая строка после окна времени в функциях `Error`
означает код ошибки. Вызов без кода и дополнительных группировок считает все ошибки.
Группировки с кодом ошибки идут после него.

```text
# В платёжном правиле: успешные выплаты пользователя внутри Party.
rule: countPayoutsSuccess("user_id", 24, hours, "party_id") > 3 -> highRisk;

# В правиле выплаты: отсутствие успешных платежей пользователя внутри Party.
rule: countSuccess("user_id", 30, days, "party_id") = 0 -> decline;

# Сумма выплат в текущей валюте, в минимальных денежных единицах.
rule: sumPayouts("user_id", 24, hours, "party_id", "currency") > 100000 -> highRisk;
```

Существующие `count`, `sum`, `unique` и их варианты продолжают читать историю
платежей. Функции `*Payouts*` всегда читают историю выплат. Shop и Wallet не
сопоставляются. `wallet_id` используется только там, где он доступен в контексте.
Область поиска задаётся явно группировками, без скрытого ограничения по Party.
`customer_id` не используется в правилах и не подставляется вместо `user_id`.

Для исполнения приложение передаёт в `AggregatorBundle` отдельные
`CountPayoutAggregator`, `SumPayoutAggregator` и `UniqueValueAggregator` для выплат.
Реализации агрегаторов отвечают за чтение истории, исключение
повторных событий и обработку отсутствующих идентификаторов. Неподтверждённая
текущая выплата не добавляется к завершённым операциям. Конвертации валют нет.
Если агрегатор не подключён или чтение истории завершилось ошибкой, действует
существующий механизм `catch:`; история платежей вместо выплат не используется.

Перед использованием правила выплаты приложение должно вызвать
`WithdrawalTemplateValidator`. Он проверяет синтаксис и запрещает `3ds` в результате
и `catch:`, а также `isTrusted`, `isMobile` и `isRecurrent`. Платёжные правила
сохраняют прежние возможности. Проверка доступности конкретных полей остаётся
ответственностью приложения и его резолверов.

Это подготовка DSL и контрактов. Подключение валидатора и агрегатора, доставка
новых полей истории и вызов `InspectWithdrawal` выполняются при интеграции сервисов.

В Thrift-контракте шаблоны, группы и привязки получают optional `operation_type`
(`payment` или `payout`). Отсутствующее значение означает `payment`. Для привязки
выплат используется `wallet_id`, для платежей — `shop_id`; тип шаблона, группы и
привязки должен совпадать. История выплат получает optional `party_id`, `wallet_id`
и `client_info` с существующими клиентскими полями.

Списки выплат используют `IdInfo.payout_id` с `party_id` и `wallet_id`. Пустой
`PayoutId` обозначает глобальный список выплат и отличается от глобального
`PaymentId`. Новые команды настройки можно публиковать после обновления
потребителей: существующие потребители ещё не обеспечивают разделение типов
операций и ключей списков.
