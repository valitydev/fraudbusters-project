---
title: operations

metatitle: operations

metadescription: operations

category: operations
---

# Функции языка

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
