# Выплаты: функциональный регресс, 2026-11-16

## Что сравнивали

| | Версия | Что внутри |
| --- | --- | --- |
| Эталон | [bc0480a8](../../versions/bc0480a8.env) | до проверки выплат |
| Кандидат | [18c71eab](../../versions/18c71eab.env) | проверка выплат, шаблоны, списки и история выплат |

## Как повторить

```bash
scripts/run-k6.sh series bc0480a8 18c71eab payments:functional payments:baseline \
    payouts:functional mixed:functional
scripts/run-k6.sh lifecycle bc0480a8 18c71eab
```

## Итог

| Набор | bc0480a8 | 18c71eab |
| --- | --- | --- |
| `payments functional`, 50 кейсов | 49 passed, 1 skipped | 48 passed, **1 failed** (PAY-FL-16), 1 skipped |
| `payments baseline` 10/с, 5 мин | 0 ошибок, p95 104 мс | 0 ошибок, p95 68 мс |
| `payouts functional`, 26 кейсов | — | 25 passed, **1 failed** (PO-API-11) |
| `mixed functional`, 4 кейса | — | 4 passed |
| `lifecycle`: перезапуск и обновление с данными | LC-01…05 passed | LC-01…06 passed |

Повтор 2026-11-17 дал те же результаты. Skipped — PAY-FL-21: глобальную привязку платежей нельзя создать через REST.

## Выводы

1. **PAY-FL-16 — принятая несовместимость.** Кандидат больше не поддерживает синтаксис
   `isTrusted(paymentsConditions(...))` и `isTrusted(withdrawalsConditions(...))`; остались `isTrusted()` и
   `isTrusted("template")`.
   - Новый шаблон с таким синтаксисом не проходит валидацию.
   - Если такой шаблон уже сохранён, после обновления правило с `isTrusted` молча выпадает, а остальные правила
     шаблона применяются.
2. **PO-API-11.** `GET /payouts-templates/filter?size=-1` отвечал 500 вместо 400. Исправлено в `1119c3f1`.
3. **Остальное поведение и наблюдения кейсов** на обеих версиях совпали.
4. **Дефекты обеих версий.** Ниже то, что ещё не исправлено. Остальные найденные дефекты исправлены в `1119c3f1`.

   | Кейс | Поведение |
   | --- | --- |
   | PAY-API-09 | `POST /payments-lists` с типом `naming` — 500 |
   | PAY-API-17 | аналитика возвращает сумму в основных единицах; `fraud-payments/count` считает все платежи |
   | PAY-FL-18, PAY-FL-25 | через REST не передаются `isRecurrent`, `isMobile`, `paymentSystem()`, `cardCategory()`; `/inspect-user` без суммы, ip, fingerprint |
   | PAY-FL-27 | `/inspect-payment` отвечает 500 на решение `trust`; явный email `UNKNOWN` приводится к `unknown` |
   | PAY-FL-28 | `catch: decline` из документации Fraudo грамматика не допускает; `notificationsRule` пустой |
   | PAY-FL-29 | `not A and B` разбирается как `not (A and B)` |
   | PAY-FL-31 | агрегат по `country_bank` не срабатывает |
   | MIX-04 | пороги суммы больше 2³¹ не срабатывают |
