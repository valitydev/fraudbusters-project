---
title: free-version

metatitle: free-version

metadescription: free-version

category: free-version
---

# Пробная версия

Для быстрого изучения системы, мы предлагаем пробную конфигурацию на основе docker-compose, которая описана в [github](https://github.com/valitydev/fraudbusters-compose).

### Запуск системы

1. ```docker-compose up -d``` - по умолчанию запуск осуществляется под x86 арзитектуру
2. ```docker-compose -f docker-compose.yml -f docker-compose-arm.yml up -d``` - возможность запуска под ARM (mac)
3. Посмотреть систему в действии на простом [примере](simple-use-case.md)

### Адреса сервисов для управления

1. Grafana (http://localhost:3000) - admin/admin
2. Keycloak (http://localhost:8080) - admin/admin
3. Fraudbusters UI (http://localhost:8989)
4. Swagger [FraudbustersApi](https://valitydev.github.io/swag-fraudbusters/) (http://localhost:9999)