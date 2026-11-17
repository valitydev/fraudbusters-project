# Регресс XYZ-598 после рефакторинга тестов, 2026-11-17

Повтор функционального регресса на тестах новой структуры: кейс — `suites/<набор>/cases/<ID>.js`, его данные —
`data/<набор>/<ID>.js`. Цель — убедиться, что после рефакторинга тесты дают те же результаты, что и
[2026-11-16](../2026-11-16-regression/README.md).

## Данные прогона

| | |
| --- | --- |
| Дата | 2026-11-17 |
| Стенд | локальный, `scripts/run-k6.sh up`, Docker Desktop 10 ГБ, каждая версия на свежем стеке без данных |
| Тесты | `tests/k6` новой структуры, данные кейсов из [data](../../data) |

| Версия | Коммит | Образы |
| --- | --- | --- |
| [before-payouts](../../versions/bc0480a8.env) | `bc0480a8` | `sha-bc0480a`, wb-list-manager `sha-4743641` |
| [after-payouts](../../versions/18c71eab.env) | `18c71eab` | `sha-306afdb`, fraudbusters-management `sha-45f6deb` |

```bash
scripts/run-k6.sh up after-payouts
scripts/run-k6.sh test after-payouts payments functional
scripts/run-k6.sh test after-payouts payouts functional
scripts/run-k6.sh test after-payouts mixed functional
scripts/run-k6.sh down
scripts/run-k6.sh up before-payouts
scripts/run-k6.sh test before-payouts payments functional
scripts/run-k6.sh lifecycle before-payouts after-payouts
node tests/k6/tools/compare.mjs <payments before-payouts> <payments after-payouts>
```

## Итог

| Набор | before-payouts | after-payouts |
| --- | --- | --- |
| `payments functional`, 50 кейсов | 49 passed, 1 skipped | 48 passed, **1 failed** (PAY-FL-16), 1 skipped |
| `payouts functional`, 26 кейсов | — | 25 passed, **1 failed** (PO-API-11) |
| `mixed functional`, 4 кейса | — | 4 passed |
| `lifecycle`, обновление с данными | LC-01…05 подготовлены, passed | LC-01…05 после обновления passed |

Результаты совпадают с прогоном 2026-11-16. `compare.mjs` между версиями показывает те же два различия:
PAY-FL-16 (снятый синтаксис `isTrusted(paymentsConditions(...))`, несовместимость принята) и новые типы
`payout_*` в справочнике аудита (PAY-API-18). PO-API-11 (`size=-1` в API выплат — 500) исправлен в ветке
`ft/XYZ-598/regression-fixes` и на опубликованных образах ещё виден.

LC-05 после обновления: шаблон с `isTrusted(paymentsConditions(...))` остаётся в management, остальные правила
шаблона применяются (`fatal` на сумме выше порога) — так же, как 2026-11-16.
