package dev.vality.fraudbusters.repository.clickhouse.query;

import dev.vality.fraudbusters.constant.EventSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PayoutHistoryQuery {

    /**
     * The latest state of each payout in the window, so replays and status changes of one payout count once.
     * When two records share the event time, the one with more of party, wallet and user filled in wins.
     */
    public static final String SELECT_LATEST_PAYOUTS = String.format("""
            (
                select id,
                       latest.1 as eventTime,
                       latest.2 as amount,
                       latest.3 as currency,
                       latest.4 as status,
                       latest.5 as errorCode,
                       latest.6 as partyId,
                       latest.7 as walletId,
                       latest.8 as userId,
                       latest.9 as cardToken,
                       latest.10 as bin,
                       latest.11 as lastDigits,
                       latest.12 as issuerCountry,
                       latest.13 as ip,
                       latest.14 as email,
                       latest.15 as phone,
                       latest.16 as fingerprint
                from (
                    select id,
                           argMax(
                               tuple(eventTime, amount, currency, status, errorCode, partyId, walletId, userId,
                                     cardToken, bin, lastDigits, issuerCountry, ip, email, phone, fingerprint),
                               tuple(eventTime, isNotNull(partyId) + isNotNull(walletId) + isNotNull(userId))
                           ) as latest
                    from %s
                    where timestamp >= ?
                    and timestamp <= ?
                    and eventTime >= ?
                    and eventTime <= ?
                    group by id
                )
            )""", EventSource.FRAUD_EVENTS_WITHDRAWAL.getTable());

}
