package dev.vality.fraudbusters.management.service;

import dev.vality.fraudbusters.management.domain.TimeSplitInfo;
import dev.vality.fraudbusters.management.service.clickhouse.AnalyticsQuery;
import dev.vality.fraudbusters.management.service.iface.BaseAnalyticsService;
import dev.vality.fraudbusters.management.service.iface.SqlTimeSplitService;
import dev.vality.fraudbusters.management.service.iface.WarehouseQueryService;
import dev.vality.fraudbusters.management.utils.ResultExtractorUtil;
import dev.vality.fraudbusters.warehouse.Query;
import dev.vality.fraudbusters.warehouse.Result;
import dev.vality.fraudbusters.warehouse.Row;
import dev.vality.swag.fraudbusters.management.model.SplitUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.vality.fraudbusters.management.constant.AnalyticsResultField.*;

@Service
@RequiredArgsConstructor
public class BaseAnalyticsServiceImpl implements BaseAnalyticsService {

    private final WarehouseQueryService warehouseQueryService;
    private final SqlTimeSplitService sqlTimeSplitService;

    @Override
    public Integer getBlockedFraudPaymentsCount(Map<String, String> params) {
        Query query = buildQuery(params, AnalyticsQuery.BLOCKED_FRAUD_PAYMENTS_COUNT);
        Result result = warehouseQueryService.execute(query);
        return ResultExtractorUtil.getIntegerField(result, COUNT);
    }

    private Query buildQuery(Map<String, String> params, String statement) {
        Query query = new Query();
        query.setParams(params);
        query.setStatement(statement);
        return query;
    }

    @Override
    public Float getBlockedFraudPaymentsCountRatio(Map<String, String> params) {
        Query query = buildQuery(params, AnalyticsQuery.BLOCKED_FRAUD_PAYMENTS_COUNT_RATIO);
        Result result = warehouseQueryService.execute(query);
        return ResultExtractorUtil.getFloatField(result, RATIO);
    }

    @Override
    public Float getBlockedFraudPaymentsSum(Map<String, String> params) {
        Query query = buildQuery(params, AnalyticsQuery.BLOCKED_FRAUD_PAYMENTS_SUM);
        Result result = warehouseQueryService.execute(query);
        return ResultExtractorUtil.getFloatField(result, SUM);
    }

    @Override
    public Integer getFraudPaymentsCount(Map<String, String> params) {
        Query query = buildQuery(params, AnalyticsQuery.FRAUD_PAYMENTS_COUNT);
        Result result = warehouseQueryService.execute(query);
        return ResultExtractorUtil.getIntegerField(result, COUNT);
    }


    @Override
    public List<Map<String, String>> getFraudPaymentsResultsSummary(Map<String, String> params) {
        Query query = buildQuery(params, AnalyticsQuery.FRAUD_PAYMENTS_RESULTS_SUMMARY);
        Result result = warehouseQueryService.execute(query);
        return result.getValues().stream()
                .map(Row::getValues)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, String>> getFraudPaymentsScoreSplitCountRatio(Map<String, String> params,
                                                                          SplitUnit splintUnit) {
        TimeSplitInfo timeSplitInfo = sqlTimeSplitService.getSplitInfo(splintUnit);
        String statement = String.format(
                AnalyticsQuery.FRAUD_PAYMENTS_SCORE_SPLIT_COUNT_RATIO,
                timeSplitInfo.getStatement(),
                timeSplitInfo.getTimeUnit()
        );
        Query query = buildQuery(params, statement);
        Result result = warehouseQueryService.execute(query);
        return result.getValues().stream()
                .map(Row::getValues)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getCurrencies() {
        Query query = buildQuery(null, AnalyticsQuery.CURRENCIES);
        Result result = warehouseQueryService.execute(query);
        return result.getValues().stream()
                .map(Row::getValues)
                .flatMap(stringStringMap -> stringStringMap.values().stream())
                .collect(Collectors.toList());
    }
}
