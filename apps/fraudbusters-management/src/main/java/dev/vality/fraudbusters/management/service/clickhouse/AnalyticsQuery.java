package dev.vality.fraudbusters.management.service.clickhouse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnalyticsQuery {

    public static final String BLOCKED_FRAUD_PAYMENTS_COUNT =
            """
                        SELECT
                            uniqExactIf(id,
                                status = 'failed'
                                AND errorCode = 'no_route_found:risk_score_is_too_high') AS count
                        FROM fraud.payment
                        WHERE
                            timestamp >= toDate(:from)
                            AND timestamp <= toDate(:to)
                            AND toDateTime(eventTime) >= toDateTime(:from)
                            AND toDateTime(eventTime) <= toDateTime(:to)
                            AND currency = :currency
                            AND like(shopId, :shopId)
                            AND like(partyId, :partyId)
                            AND shopId != 'TEST'
                    """;

    public static final String BLOCKED_FRAUD_PAYMENTS_COUNT_RATIO =
            """
               SELECT
                  uniqExactIf(id,
                      status = 'failed'
                      AND errorCode = 'no_route_found:risk_score_is_too_high') / uniqExact(id) AS ratio
               FROM fraud.payment
               WHERE
                  timestamp >= toDate(:from)
                  AND timestamp <= toDate(:to)
                  AND toDateTime(eventTime) >= toDateTime(:from)
                  AND toDateTime(eventTime) <= toDateTime(:to)
                  AND currency = :currency
                  AND like(shopId, :shopId)
                  AND like(partyId, :partyId)
                  AND shopId != 'TEST'
            """;

    public static final String BLOCKED_FRAUD_PAYMENTS_SUM =
            """
                        SELECT
                            sum(amount) / 100 AS sum
                        FROM fraud.payment
                        WHERE
                            timestamp >= toDate(:from)
                            AND timestamp <= toDate(:to)
                            AND toDateTime(eventTime) >= toDateTime(:from)
                            AND toDateTime(eventTime) <= toDateTime(:to)
                            AND status = 'failed'
                            AND errorCode='no_route_found:risk_score_is_too_high'
                            AND currency = :currency
                            AND like(shopId, :shopId)
                            AND like(partyId, :partyId)
                            AND shopId != 'TEST'
                    """;

    public static final String FRAUD_PAYMENTS_COUNT =
            """
                        SELECT
                            uniqExact(id) AS count
                        FROM fraud.payment
                        WHERE
                            timestamp >= toDate(:from)
                            AND timestamp <= toDate(:to)
                            AND toDateTime(eventTime) >= toDateTime(:from)
                            AND toDateTime(eventTime) <= toDateTime(:to)
                            AND currency = :currency
                            AND like(shopId, :shopId)
                            AND like(partyId, :partyId)
                            AND shopId != 'TEST'
                    """;

    public static final String FRAUD_PAYMENTS_RESULTS_SUMMARY =
            """
                        SELECT
                            resultStatus AS status,
                            checkedRule AS rule,
                            checkedTemplate AS template,
                            count() AS count,
                            sum(amount / 100) AS sum,
                            count() /(SELECT
                                          count() AS all
                                      FROM fraud.events_unique
                                      WHERE
                                          timestamp >= toDate(:from)
                                          AND timestamp <= toDate(:to)
                                          AND toDateTime(eventTime) >= toDateTime(:from)
                                          AND toDateTime(eventTime) <= toDateTime(:to)
                                          AND currency = :currency
                                          AND like(shopId, :shopId)
                                          AND like(partyId, :partyId)
                                          AND shopId != 'TEST') AS ratio
                        FROM fraud.events_unique
                        WHERE
                            timestamp >= toDate(:from)
                            AND timestamp <= toDate(:to)
                            AND toDateTime(eventTime) >= toDateTime(:from)
                            AND toDateTime(eventTime) <= toDateTime(:to)
                            AND currency = :currency
                            AND like(shopId, :shopId)
                            AND like(partyId, :partyId)
                            AND shopId != 'TEST'
                        GROUP BY status, rule, template
                    """;

    public static final String FRAUD_PAYMENTS_SCORE_SPLIT_COUNT_RATIO =
            """
                        SELECT
                            %2$s,
                            trusted_count * 100 / total_count AS trusted,
                            low_count * 100 / total_count AS low,
                            (total_count - trusted_count - low_count - fatal_count) * 100 / total_count AS high,
                            fatal_count * 100 / total_count AS fatal
                        FROM
                        (
                            SELECT
                                %1$s,
                                countIf(resultStatus = 'TRUST') AS trusted_count,
                                countIf(resultStatus IN ('ACCEPT', 'ACCEPT_AND_NOTIFY', 'NOTIFY')) AS low_count,
                                countIf(resultStatus IN ('DECLINE', 'DECLINE_AND_NOTIFY')) AS fatal_count,
                                count() AS total_count
                            FROM fraud.events_unique
                            WHERE
                                timestamp >= toDate(:from)
                                AND timestamp <= toDate(:to)
                                AND toDateTime(eventTime) >= toDateTime(:from)
                                AND toDateTime(eventTime) <= toDateTime(:to)
                                and shopId!='TEST'
                                AND currency = :currency
                                AND like(shopId, :shopId)
                                AND like(partyId, :partyId)
                            GROUP BY %2$s
                            ORDER BY %2$s
                        )
                    """;

    public static final String CURRENCIES =
            """
                        SELECT DISTINCT
                            currency
                        FROM fraud.payment
                    """;
}
