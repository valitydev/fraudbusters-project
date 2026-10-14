---
title: simple-use-case

metatitle: simple-use-case

metadescription: simple-use-case

category: simple-use-case
---

# Простой сценарий использования

Необходимо [преднастроить](./simple-config.md) систему для запуска теста

#### Схема взаимодействия:

<img alt="svg" src="../img/fraudbusters sequence diagram.svg" /></p>

#### Настройка правил в системе:

1. Идем в пользовательский интерфейс ```localhost:8989```
2. Авторизуемся под настроенным пользователем
3. Идем в **Templates** -> **Create template** 
4. Создаем template _name_: ```test``` _template_: ```rule:test:amount() > 100 -> decline;```
5. Идем в Reference -> Create reference 
6. Создаем связь _Template id_: ```test``` _Party id_: ```partyTest``` _Shop id_: ```shopTest```

#### Проверка срабатывания правил:

1. Дальше можно воспользоваться специальным тестовым [сервисом](https://github.com/rbkmoney/fraudbusters-examples) для эмуляции системы платежей
2. Увидеть результаты можно в **Historical data** -> **Inspect result**
3. Также после прохождения теста появятся данные на странице **Analytics**