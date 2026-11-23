package dev.vality.fraudbusters.repository.clickhouse.impl;

import dev.vality.fraudbusters.domain.PayoutResult;
import dev.vality.fraudbusters.repository.Repository;
import dev.vality.fraudbusters.repository.clickhouse.mapper.PayoutCheckMapper;
import dev.vality.fraudbusters.repository.clickhouse.query.PayoutCheckQuery;
import dev.vality.fraudbusters.repository.clickhouse.setter.PayoutCheckBatchPreparedStatementSetter;
import dev.vality.fraudbusters.repository.clickhouse.util.FilterUtil;
import dev.vality.fraudbusters.service.dto.FilterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutCheckRepository implements Repository<PayoutResult> {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final PayoutCheckMapper payoutCheckMapper;

    @Override
    public void insert(PayoutResult payoutResult) {
        insertBatch(List.of(payoutResult));
    }

    @Override
    public void insertBatch(List<PayoutResult> batch) {
        if (batch != null && !batch.isEmpty()) {
            log.debug("PayoutCheckRepository insertBatch size: {}", batch.size());
            jdbcTemplate.batchUpdate(
                    PayoutCheckBatchPreparedStatementSetter.INSERT,
                    new PayoutCheckBatchPreparedStatementSetter(batch)
            );
        }
    }

    @Override
    public List<PayoutResult> getByFilter(FilterDto filter) {
        String query = PayoutCheckQuery.SELECT_HISTORY_PAYOUT_CHECK +
                FilterUtil.appendFilters(filter);
        MapSqlParameterSource params = FilterUtil.initParams(filter);
        return namedParameterJdbcTemplate.query(query, params, payoutCheckMapper);
    }

}
