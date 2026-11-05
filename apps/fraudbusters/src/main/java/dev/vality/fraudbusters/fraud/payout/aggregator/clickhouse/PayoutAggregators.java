package dev.vality.fraudbusters.fraud.payout.aggregator.clickhouse;

import dev.vality.fraudbusters.exception.RuleFunctionException;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.OperationModel;
import dev.vality.fraudbusters.fraud.resolver.HistoryFieldResolver;
import dev.vality.fraudbusters.repository.clickhouse.impl.PayoutHistoryRepository;
import dev.vality.fraudbusters.service.TimeBoundaryService;
import dev.vality.fraudbusters.util.TimestampUtil;
import dev.vality.fraudo.aggregator.UniqueValueAggregator;
import dev.vality.fraudo.model.TimeWindow;
import dev.vality.fraudo.payout.aggregator.CountPayoutAggregator;
import dev.vality.fraudo.payout.aggregator.SumPayoutAggregator;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Payout history aggregates; payment and payout rules can both use them. */
@RequiredArgsConstructor
public class PayoutAggregators<T extends OperationModel, U> implements CountPayoutAggregator<T, U>,
        SumPayoutAggregator<T, U>,
        UniqueValueAggregator<T, U> {

    private final PayoutHistoryRepository repository;
    private final TimeBoundaryService timeBoundaryService;
    private final HistoryFieldResolver<T, U> fields;

    @Override
    public Integer count(U field, T model, TimeWindow window,
                         List<U> groups) {
        return (int) aggregate("count()", field, model, window, groups, null, null);
    }

    @Override
    public Integer countSuccess(U field, T model, TimeWindow window,
                                List<U> groups) {
        return (int) aggregate("count()", field, model, window, groups, "succeeded", null);
    }

    @Override
    public Integer countError(U field, T model, TimeWindow window,
                              List<U> groups) {
        return countError(field, model, window, null, groups);
    }

    @Override
    public Integer countError(U field, T model, TimeWindow window,
                              String errorCode, List<U> groups) {
        return (int) aggregate("count()", field, model, window, groups, "failed", errorCode);
    }

    @Override
    public Double sum(U field, T model, TimeWindow window,
                      List<U> groups) {
        return aggregate("sum(amount)", field, model, window, groups, null, null);
    }

    @Override
    public Double sumSuccess(U field, T model, TimeWindow window,
                             List<U> groups) {
        return aggregate("sum(amount)", field, model, window, groups, "succeeded", null);
    }

    @Override
    public Double sumError(U field, T model, TimeWindow window,
                           List<U> groups) {
        return sumError(field, model, window, null, groups);
    }

    @Override
    public Double sumError(U field, T model, TimeWindow window,
                           String errorCode, List<U> groups) {
        return aggregate("sum(amount)", field, model, window, groups, "failed", errorCode);
    }

    @Override
    public Integer countUniqueValue(U field, T model, U uniqueField,
                                    TimeWindow window, List<U> groups) {
        return (int) aggregate("uniqExact(" + fields.resolve(uniqueField) + ")", field, model, window, groups,
                null, null);
    }

    private double aggregate(String expression, U field, T model, TimeWindow window,
                             List<U> groups, String status, String errorCode) {
        try {
            List<FieldModel> filters = new ArrayList<>();
            filters.add(fields.resolve(field, model));
            if (groups != null) {
                groups.forEach(group -> filters.add(fields.resolve(group, model)));
            }
            boolean hasMissingValue = filters.stream()
                    .anyMatch(filter -> filter.getValue() == null
                            || filter.getValue() instanceof String value && value.isBlank());
            if (hasMissingValue) {
                return 0;
            }
            var bound = timeBoundaryService.getBoundary(TimestampUtil.instantFromModel(model), window);
            return repository.aggregate(expression, filters, bound, status, errorCode);
        } catch (Exception e) {
            throw new RuleFunctionException(e);
        }
    }
}
