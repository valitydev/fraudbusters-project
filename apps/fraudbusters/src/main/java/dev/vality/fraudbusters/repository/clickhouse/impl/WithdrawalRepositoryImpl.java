package dev.vality.fraudbusters.repository.clickhouse.impl;

import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.fraudbusters.constant.EventSource;
import dev.vality.fraudbusters.repository.Repository;
import dev.vality.fraudbusters.repository.clickhouse.mapper.WithdrawalMapper;
import dev.vality.fraudbusters.repository.clickhouse.query.WithdrawalQuery;
import dev.vality.fraudbusters.repository.clickhouse.setter.WithdrawalBatchPreparedStatementSetter;
import dev.vality.fraudbusters.repository.clickhouse.util.FilterUtil;
import dev.vality.fraudbusters.service.dto.FilterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Primary
@Profile("full-prod")
@Component
@RequiredArgsConstructor
public class WithdrawalRepositoryImpl implements Repository<Withdrawal> {

    private static final String INSERT = String.format(
            "INSERT INTO %1s (%2s) VALUES (%3s)",
            EventSource.FRAUD_EVENTS_WITHDRAWAL.getTable(),
            WithdrawalBatchPreparedStatementSetter.FIELDS,
            WithdrawalBatchPreparedStatementSetter.FIELDS_MARK
    );
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final WithdrawalMapper withdrawalMapper;

    @Override
    public void insert(Withdrawal payment) {
        throw new UnsupportedOperationException("Method insert is not support!");
    }

    @Override
    public void insertBatch(List<Withdrawal> batch) {
        if (batch != null && !batch.isEmpty()) {
            log.debug("insertBatch batch withdrawals: {}", batch);
            jdbcTemplate.batchUpdate(INSERT, new WithdrawalBatchPreparedStatementSetter(batch));
        }
    }

    @Override
    public List<Withdrawal> getByFilter(FilterDto filter) {
        String query = WithdrawalQuery.SELECT_HISTORY_WITHDRAWAL + FilterUtil.appendFilters(filter);
        MapSqlParameterSource params = FilterUtil.initParams(filter);
        return namedParameterJdbcTemplate.query(query, params, withdrawalMapper);
    }

}
