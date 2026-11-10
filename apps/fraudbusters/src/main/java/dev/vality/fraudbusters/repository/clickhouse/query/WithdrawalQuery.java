package dev.vality.fraudbusters.repository.clickhouse.query;

import dev.vality.fraudbusters.constant.EventSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WithdrawalQuery {

    /**
     * The latest state of each payout in the period, one row per payout, as PayoutHistoryQuery picks it for rules.
     * Search filters, sorting and paging are appended after the final WHERE and apply to that latest state.
     */
    public static final String SELECT_HISTORY_WITHDRAWAL = String.format("""
            SELECT *
            FROM (
                SELECT id,
                       latest.1 as eventTime,
                       latest.2 as amount,
                       latest.3 as currency,
                       latest.4 as status,
                       latest.5 as errorCode,
                       latest.6 as errorReason,
                       latest.7 as partyId,
                       latest.8 as walletId,
                       latest.9 as customerId,
                       latest.10 as userId,
                       latest.11 as cardToken,
                       latest.12 as bin,
                       latest.13 as lastDigits,
                       latest.14 as paymentSystem,
                       latest.15 as issuerCountry,
                       latest.16 as terminal,
                       latest.17 as providerId,
                       latest.18 as bankCountry,
                       latest.19 as accountId,
                       latest.20 as accountCurrency,
                       latest.21 as ip,
                       latest.22 as fingerprint,
                       latest.23 as email,
                       latest.24 as phone
                FROM (
                    SELECT id,
                           argMax(
                               tuple(eventTime, amount, currency, status, errorCode, errorReason, partyId, walletId,
                                     customerId, userId, cardToken, bin, lastDigits, paymentSystem, issuerCountry,
                                     terminal, providerId, bankCountry, accountId, accountCurrency, ip, fingerprint,
                                     email, phone),
                               tuple(eventTime, isNotNull(partyId) + isNotNull(walletId) + isNotNull(userId))
                           ) as latest
                    FROM %s
                    WHERE timestamp >= toDate(:from)
                      and timestamp <= toDate(:to)
                      and toDateTime(eventTime) >= toDateTime(:from)
                      and toDateTime(eventTime) <= toDateTime(:to)
                    GROUP BY id
                )
            )
            WHERE 1 = 1""", EventSource.FRAUD_EVENTS_WITHDRAWAL.getTable());

}
