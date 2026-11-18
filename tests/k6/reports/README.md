# Отчёты прогонов

Каждый прогон — отдельная папка `<дата>-<вид>`; в её `README.md` дата, версии с тегами образов, состояние тестов,
команды и итог. Сырые результаты прогонов (`tests/k6/results`) в git не хранятся.

Версии `before-payouts` и `after-payouts` в отчётах ноября 2026 — нынешние
[versions/bc0480a8.env](../versions/bc0480a8.env) и [versions/18c71eab.env](../versions/18c71eab.env).

| Дата | Прогон | Версии | Итог |
| --- | --- | --- | --- |
| 2026-11-16 | [Регресс XYZ-598](2026-11-16-regression/README.md) | before-payouts, after-payouts | регресс PAY-FL-16 (принят), дефект PO-API-11 |
| 2026-11-16 — 2026-11-17 | [Нагрузка](2026-11-17-load/README.md) | before-payouts, after-payouts | регресса задержки в коде не найдено |
| 2026-11-17 | [Регресс после рефакторинга тестов](2026-11-17-regression/README.md) | before-payouts, after-payouts | результаты совпали с 2026-11-16 |
| 2026-11-17 — 2026-11-18 | [ClickHouse 25.7.5, драйвер, Kafka KRaft](2026-11-18-clickhouse-25/README.md) | 1119c3f1, f8049ae0 | регресса нет; без пула драйвер непригоден, с пулом `load` быстрее |
