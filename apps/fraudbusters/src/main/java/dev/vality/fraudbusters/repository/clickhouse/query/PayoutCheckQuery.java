package dev.vality.fraudbusters.repository.clickhouse.query;

import dev.vality.fraudbusters.constant.EventSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PayoutCheckQuery {

    public static final String SELECT_HISTORY_PAYOUT_CHECK = String.format("""
            SELECT
                eventTime,
                id,
                partyId,
                walletId,
                amount,
                currency,
                cardToken,
                bin,
                lastDigits,
                paymentSystem,
                issuerCountry,
                customerId,
                userId,
                ip,
                fingerprint,
                email,
                phone,
                resultStatus,
                checkedRule,
                checkedTemplate
            FROM
            %s
            WHERE
                timestamp >= toDate(:from)
                and timestamp <= toDate(:to)
                and toDateTime(eventTime) >= toDateTime(:from)
                and toDateTime(eventTime) <= toDateTime(:to)""", EventSource.FRAUD_PAYOUT_CHECK.getTable());

}
