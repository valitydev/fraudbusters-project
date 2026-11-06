package dev.vality.fraudbusters.repository.clickhouse.impl;

import dev.vality.damsel.fraudbusters.WithdrawalStatus;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.repository.clickhouse.extractor.CountExtractor;
import dev.vality.fraudbusters.repository.clickhouse.extractor.SumExtractor;
import dev.vality.fraudbusters.repository.clickhouse.query.PayoutHistoryQuery;
import dev.vality.fraudbusters.repository.clickhouse.util.AggregationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PayoutHistoryRepository {

    private static final List<String> FINAL_STATUSES = List.of(
            WithdrawalStatus.succeeded.name(),
            WithdrawalStatus.failed.name()
    );

    private final JdbcTemplate jdbcTemplate;

    public Integer countOperationByFieldWithGroupBy(
            String fieldName, Object value, Long from, Long to,
            List<FieldModel> fieldModels) {
        String sql = buildSql(fieldName, "count() as cnt", "status in (?, ?)", fieldModels);
        List<Object> params = AggregationUtil.generateStatusesParams(from, to, fieldModels, value, FINAL_STATUSES);
        log.debug("PayoutHistoryRepository countOperationByFieldWithGroupBy sql: {}", sql);
        return jdbcTemplate.query(sql, params.toArray(), new CountExtractor());
    }

    public Integer countOperationSuccessWithGroupBy(
            String fieldName, Object value, Long from, Long to,
            List<FieldModel> fieldModels) {
        String sql = buildSql(fieldName, "count() as cnt", "status = ?", fieldModels);
        List<Object> params =
                AggregationUtil.generateParams(from, to, fieldModels, value, WithdrawalStatus.succeeded.name());
        log.debug("PayoutHistoryRepository countOperationSuccessWithGroupBy sql: {}", sql);
        return jdbcTemplate.query(sql, params.toArray(), new CountExtractor());
    }

    public Integer countOperationErrorWithGroupBy(
            String fieldName, Object value, Long from, Long to,
            List<FieldModel> fieldModels) {
        String sql = buildSql(fieldName, "count() as cnt", "status = ?", fieldModels);
        List<Object> params =
                AggregationUtil.generateParams(from, to, fieldModels, value, WithdrawalStatus.failed.name());
        log.debug("PayoutHistoryRepository countOperationErrorWithGroupBy sql: {}", sql);
        return jdbcTemplate.query(sql, params.toArray(), new CountExtractor());
    }

    public Integer countOperationErrorWithGroupBy(
            String fieldName, Object value, Long from, Long to,
            List<FieldModel> fieldModels, String errorCode) {
        String sql = buildSql(fieldName, "count() as cnt", "status = ? and errorCode = ?", fieldModels);
        List<Object> params = AggregationUtil.generateParams(from, to, fieldModels, value,
                WithdrawalStatus.failed.name(), errorCode);
        log.debug("PayoutHistoryRepository countOperationErrorWithGroupBy sql: {}", sql);
        return jdbcTemplate.query(sql, params.toArray(), new CountExtractor());
    }

    public Long sumOperationByFieldWithGroupBy(
            String fieldName, Object value, Long from, Long to,
            List<FieldModel> fieldModels) {
        String sql = buildSql(fieldName, "sum(amount) as sum", "status in (?, ?)", fieldModels);
        List<Object> params = AggregationUtil.generateStatusesParams(from, to, fieldModels, value, FINAL_STATUSES);
        log.debug("PayoutHistoryRepository sumOperationByFieldWithGroupBy sql: {}", sql);
        return jdbcTemplate.query(sql, params.toArray(), new SumExtractor());
    }

    public Long sumOperationSuccessWithGroupBy(
            String fieldName, Object value, Long from, Long to,
            List<FieldModel> fieldModels) {
        String sql = buildSql(fieldName, "sum(amount) as sum", "status = ?", fieldModels);
        List<Object> params =
                AggregationUtil.generateParams(from, to, fieldModels, value, WithdrawalStatus.succeeded.name());
        log.debug("PayoutHistoryRepository sumOperationSuccessWithGroupBy sql: {}", sql);
        return jdbcTemplate.query(sql, params.toArray(), new SumExtractor());
    }

    public Long sumOperationErrorWithGroupBy(
            String fieldName, Object value, Long from, Long to,
            List<FieldModel> fieldModels) {
        String sql = buildSql(fieldName, "sum(amount) as sum", "status = ?", fieldModels);
        List<Object> params =
                AggregationUtil.generateParams(from, to, fieldModels, value, WithdrawalStatus.failed.name());
        log.debug("PayoutHistoryRepository sumOperationErrorWithGroupBy sql: {}", sql);
        return jdbcTemplate.query(sql, params.toArray(), new SumExtractor());
    }

    public Long sumOperationErrorWithGroupBy(
            String fieldName, Object value, Long from, Long to,
            List<FieldModel> fieldModels, String errorCode) {
        String sql = buildSql(fieldName, "sum(amount) as sum", "status = ? and errorCode = ?", fieldModels);
        List<Object> params = AggregationUtil.generateParams(from, to, fieldModels, value,
                WithdrawalStatus.failed.name(), errorCode);
        log.debug("PayoutHistoryRepository sumOperationErrorWithGroupBy sql: {}", sql);
        return jdbcTemplate.query(sql, params.toArray(), new SumExtractor());
    }

    public Integer uniqCountOperationWithGroupBy(
            String fieldNameBy, Object value, String fieldNameCount, Long from, Long to,
            List<FieldModel> fieldModels) {
        String sql = buildSql(fieldNameBy, String.format("uniqExact(%s) as cnt", fieldNameCount),
                "status in (?, ?)", fieldModels);
        List<Object> params = AggregationUtil.generateStatusesParams(from, to, fieldModels, value, FINAL_STATUSES);
        log.debug("PayoutHistoryRepository uniqCountOperationWithGroupBy sql: {}", sql);
        return jdbcTemplate.query(sql, params.toArray(), new CountExtractor());
    }

    private String buildSql(String fieldName, String aggregate, String statusCondition,
                            List<FieldModel> fieldModels) {
        StringBuilder sql = new StringBuilder(String.format("""
                select %1$s, %2$s
                from %3$s
                where %1$s = ? and %4$s""",
                fieldName,
                aggregate,
                PayoutHistoryQuery.SELECT_LATEST_PAYOUTS,
                statusCondition
        ));
        StringBuilder sqlGroupBy = new StringBuilder(String.format(" group by %1$s", fieldName));
        return AggregationUtil.appendGroupingFields(fieldModels, sql, sqlGroupBy)
                .toString();
    }
}
