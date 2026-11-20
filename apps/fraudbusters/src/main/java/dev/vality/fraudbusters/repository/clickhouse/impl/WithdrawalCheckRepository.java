package dev.vality.fraudbusters.repository.clickhouse.impl;

import dev.vality.fraudbusters.domain.PayoutResult;
import dev.vality.fraudbusters.repository.Repository;
import dev.vality.fraudbusters.repository.clickhouse.mapper.WithdrawalCheckMapper;
import dev.vality.fraudbusters.repository.clickhouse.query.WithdrawalCheckQuery;
import dev.vality.fraudbusters.repository.clickhouse.setter.WithdrawalCheckBatchPreparedStatementSetter;
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
public class WithdrawalCheckRepository implements Repository<PayoutResult> {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final WithdrawalCheckMapper withdrawalCheckMapper;

    @Override
    public void insert(PayoutResult payoutResult) {
        insertBatch(List.of(payoutResult));
    }

    @Override
    public void insertBatch(List<PayoutResult> batch) {
        if (batch != null && !batch.isEmpty()) {
            log.debug("WithdrawalCheckRepository insertBatch size: {}", batch.size());
            jdbcTemplate.batchUpdate(
                    WithdrawalCheckBatchPreparedStatementSetter.INSERT,
                    new WithdrawalCheckBatchPreparedStatementSetter(batch)
            );
        }
    }

    @Override
    public List<PayoutResult> getByFilter(FilterDto filter) {
        String query = WithdrawalCheckQuery.SELECT_HISTORY_WITHDRAWAL_CHECK +
                FilterUtil.appendFilters(filter);
        MapSqlParameterSource params = FilterUtil.initParams(filter);
        return namedParameterJdbcTemplate.query(query, params, withdrawalCheckMapper);
    }

}
