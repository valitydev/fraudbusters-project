---
title: simple-config

metatitle: simple-config

metadescription: simple-config

category: simple-config
---

# Первичная настройка системы:

1. Проверяем функционирование всех контейнеров
2. Заходим в keycloak ```http://localhost:8080```
3. Идем в админ консоль login: ```admin``` password: ```admin```
<img alt="svg" src="../img/compose-usage/keycloak-admin.png" /></p>
4. Нажимаем на кнопку **Add realm**

<img alt="svg" src="../img/compose-usage/add-realm.png" /></p>
5. Загружаем подготовленный файл конфигурации - ```keycloak/realm-export.json```
<img alt="svg" src="../img/compose-usage/add-realm-2.png" /></p>
6. Добавляем паттерн для **web origin**  
<img alt="svg" src="../img/compose-usage/add-web-origin-pattern.png" /></p>
7. Идем в управление пользователями  
<img alt="svg" src="../img/compose-usage/go-user.png" /></p>
8. Нажимаем на **Add user**
9. Вводим имя пользователя
10. Переходим на вкладку **Credentials**
<img alt="svg" src="../img/compose-usage/set-cred.png" /></p>
11. Переходим в роли и устанавливаем их, как на скриншоте
<img alt="svg" src="../img/compose-usage/add-role.png" /></p>
12. При необходимости добавляем **/etc/hosts** **127.0.0.1** **keycloak**