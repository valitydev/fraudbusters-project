package dev.vality.fraudbusters.repository.clickhouse.impl;

import dev.vality.fraudbusters.domain.TimeBound;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.sql.Date;
import java.time.ZoneOffset;

/** Resolves replays and status changes before applying rule filters. */
@Repository
@RequiredArgsConstructor
public class PayoutHistoryRepository {

    private static final String HISTORY = """
            FROM (
                SELECT id,
                       latest.1 AS eventTime, latest.2 AS amount, latest.3 AS currency,
                       latest.4 AS status, latest.5 AS errorCode, latest.6 AS partyId,
                       latest.7 AS walletId, latest.8 AS userId, latest.9 AS cardToken,
                       latest.10 AS bin, latest.11 AS lastDigits, latest.12 AS issuerCountry,
                       latest.13 AS ip, latest.14 AS email, latest.15 AS phone,
                       latest.16 AS fingerprint
                FROM (
                    SELECT id, argMax(tuple(eventTime, amount, currency, status, errorCode,
                                            partyId, walletId, userId, cardToken, bin, lastDigits,
                                            issuerCountry, ip, email, phone, fingerprint),
                                      tuple(eventTime, isNotNull(partyId) + isNotNull(walletId)
                                            + isNotNull(userId))) AS latest
                    FROM fraud.withdrawal
                    WHERE timestamp >= :fromDate AND timestamp <= :toDate
                      AND eventTime >= :from AND eventTime <= :to
                    GROUP BY id
                )
            )
            WHERE eventTime >= :from AND eventTime <= :to
              AND status IN ('succeeded', 'failed')
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public double aggregate(String expression, List<FieldModel> filters, TimeBound bound,
                            String status, String errorCode) {
        StringBuilder sql = new StringBuilder("SELECT ").append(expression).append(' ').append(HISTORY);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("from", bound.getLeft().getEpochSecond())
                .addValue("to", bound.getRight().getEpochSecond())
                .addValue("fromDate", Date.valueOf(bound.getLeft().atOffset(ZoneOffset.UTC).toLocalDate()))
                .addValue("toDate", Date.valueOf(bound.getRight().atOffset(ZoneOffset.UTC).toLocalDate()));
        for (int i = 0; i < filters.size(); i++) {
            FieldModel filter = filters.get(i);
            sql.append(" AND ").append(filter.getName()).append(" = :field").append(i);
            params.addValue("field" + i, filter.getValue());
        }
        if (status != null) {
            sql.append(" AND status = :status");
            params.addValue("status", status);
        }
        if (errorCode != null) {
            sql.append(" AND errorCode = :errorCode");
            params.addValue("errorCode", errorCode);
        }
        Number result = jdbcTemplate.queryForObject(sql.toString(), params, Number.class);
        return result == null ? 0 : result.doubleValue();
    }
}
