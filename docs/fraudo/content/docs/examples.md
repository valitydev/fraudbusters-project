---
title: examples

metatitle: examples

metadescription: examples

category: examples
---

# Примеры

####Простой пример с логическим оператором **И**:

```sh
rule: 
    3 > 2 AND 1 = 1 
-> accept;
```

####Пример работы с черными списками:

```sh
rule: 
  inBlackList("email")
-> notify;
```

данная функция может принимать список параметров:

```sh
rule: 
  inBlackList("email", "fingerprint", "card_token", "bin", "ip")
-> notify;
```

####Работа с белыми списками аналогична:

```sh
rule: check_in_white_list_rule: 
  inWhiteList("email", "fingerprint", "card_token", "bin", "ip")
-> notify;
```

####Работа с счетчиками:

```sh
rule: 
  (count("ip", 1, days) >= 10 OR countSuccess("email", 1, days) > 5)
        AND countError("fingerprint", 1, calendar_days, "error_code") > 5
-> notify;
```

Вызываются функции подсчета количества операций в разрезе ip, 
подсчет успешных платежей в разрезе email, а также количество конкретных ошибок при оплатах.

**_Это работает следующим образом_** - _если количество платежей на один ip в сутки >= 10 
или количество успешных платежей на один mail за сутки > 5 
и при выполнении одного из этих условий количество ошибок с кодом "error_code" на один fingerprint за календарнык сутки > 5, 
будет выслана уведомление_.

Работа с функциями [сумм](../operations/#summ) производится аналогично.

####Количество уникальных ip на один email

```sh
rule: unique("email", "ip") < 4
-> decline;
```

####Определение страны по ip

```sh
rule: country() = "RU"
-> notify;
```

####Определение суммы платежа

```sh
rule: amount() < 100
-> accept;
```

####Работа с обработчиком ошибок

```sh
rule: 
  unique("email", "ip") < 4 -> accept
catch: decline;
```

В случае технической ошибки во время работы данной функции транзакция будет помечена как ```decline```,
по умолчанию система возвращает ```normal``` и продолжает проверять правила

####Определение доверенного трафика

```sh
rule: isTrusted() -> accept;
rule: isTrusted("trusted-template") -> accept;
```

Первый вариант проверяет доверенность карты, второй — по именованному шаблону
trusted-tokens. Условия по истории операций задаются обычными функциями
`countSuccess`, `sumSuccess`, `countPayoutsSuccess`, `sumPayoutsSuccess`
и логическими операциями.

####Определение типа карты

```sh
rule: 
  cardCategory() = "credit"
-> accept;
```

####Определение платежной системы

```sh
rule: 
  paymentSystem() = "VISA"
-> accept;
```

####Определение рекурентного платежа

```sh
rule: 
  isRecurrent()
-> accept;
```


####Определение мобильного платежа

```sh
rule: 
  isMobile()
-> accept;
```


####Сравнение по шаблону и в списке

```sh
rule: cb:
  like("bin", "5536.*") AND in("pan", "9137", "1231")
-> decline;
```
Регулярные выражения формируются в стиле [java](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)

####Пример шаблона с несколькими правилами

```sh
# принимаем платеж, если хотя бы один из указанных параметров находится в вайтлисте
rule: inWhiteList("email", "fingerprint", "card", "bin", "ip") 
-> accept; 

# отклоняем платеж, если хотя бы один из указанных параметров находится в блэклисте
rule: inBlackList("email", "fingerprint", "card", "bin", "ip") 
-> decline; 

# эти страны блочим всегда
rule: in(countryBy("bin"), "AS", "SD", "TR", "WE", "SD", "CD", "KL", "EW", "VF", "XZ", "CD") 
-> decline; 

# лимит суммы платежа 10 баксов для 
rule: amount() > 1000 AND in(countryBy("bin"), "DS", "LA", "AS") 
-> decline; 

# лимит суммы платежа 10 баксов для некоторых стран
rule: amount() > 1000 AND in(countryBy("bin"), "VC", "WE") 
-> decline;

# лимит по сумме платежа > 100 условных единиц в валюте платежа
rule: amount() > 10000 
-> decline;
```