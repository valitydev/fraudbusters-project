package dev.vality.fraudbusters.fraud.payout.aggregator.clickhouse;

import dev.vality.fraudbusters.domain.TimeBound;
import dev.vality.fraudbusters.exception.RuleFunctionException;
import dev.vality.fraudbusters.fraud.AggregateGroupingFunction;
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

import java.time.Instant;
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
        return aggregate(field, model, window, groups, 0, repository::countOperationByFieldWithGroupBy);
    }

    @Override
    public Integer countSuccess(U field, T model, TimeWindow window,
                                List<U> groups) {
        return aggregate(field, model, window, groups, 0, repository::countOperationSuccessWithGroupBy);
    }

    @Override
    public Integer countError(U field, T model, TimeWindow window,
                              List<U> groups) {
        return aggregate(field, model, window, groups, 0, repository::countOperationErrorWithGroupBy);
    }

    @Override
    public Integer countError(U field, T model, TimeWindow window,
                              String errorCode, List<U> groups) {
        return aggregate(
                field,
                model,
                window,
                groups,
                0,
                (fieldName, value, from, to, fieldModels) -> repository.countOperationErrorWithGroupBy(
                        fieldName, value, from, to, fieldModels, errorCode)
        );
    }

    @Override
    public Double sum(U field, T model, TimeWindow window,
                      List<U> groups) {
        return Double.valueOf(aggregate(field, model, window, groups, 0L,
                repository::sumOperationByFieldWithGroupBy));
    }

    @Override
    public Double sumSuccess(U field, T model, TimeWindow window,
                             List<U> groups) {
        return Double.valueOf(aggregate(field, model, window, groups, 0L,
                repository::sumOperationSuccessWithGroupBy));
    }

    @Override
    public Double sumError(U field, T model, TimeWindow window,
                           List<U> groups) {
        return Double.valueOf(aggregate(field, model, window, groups, 0L,
                repository::sumOperationErrorWithGroupBy));
    }

    @Override
    public Double sumError(U field, T model, TimeWindow window,
                           String errorCode, List<U> groups) {
        return Double.valueOf(aggregate(
                field,
                model,
                window,
                groups,
                0L,
                (fieldName, value, from, to, fieldModels) -> repository.sumOperationErrorWithGroupBy(
                        fieldName, value, from, to, fieldModels, errorCode)
        ));
    }

    @Override
    public Integer countUniqueValue(U field, T model, U uniqueField,
                                    TimeWindow window, List<U> groups) {
        return aggregate(
                field,
                model,
                window,
                groups,
                0,
                (fieldName, value, from, to, fieldModels) -> repository.uniqCountOperationWithGroupBy(
                        fieldName, value, fields.resolve(uniqueField), from, to, fieldModels)
        );
    }

    private <R> R aggregate(
            U field,
            T model,
            TimeWindow window,
            List<U> groups,
            R emptyResult,
            AggregateGroupingFunction<String, Object, Long, Long, List<FieldModel>, R> aggregateFunction) {
        try {
            FieldModel resolve = fields.resolve(field, model);
            List<FieldModel> eventFields = fields.resolveListFields(model, groups);
            if (isMissing(resolve) || eventFields.stream().anyMatch(this::isMissing)) {
                return emptyResult;
            }
            Instant timestamp = TimestampUtil.instantFromModel(model);
            TimeBound timeBound = timeBoundaryService.getBoundary(timestamp, window);
            return aggregateFunction.accept(
                    resolve.getName(),
                    resolve.getValue(),
                    timeBound.getLeft().toEpochMilli(),
                    timeBound.getRight().toEpochMilli(),
                    eventFields
            );
        } catch (Exception e) {
            throw new RuleFunctionException(e);
        }
    }

    private boolean isMissing(FieldModel fieldModel) {
        return fieldModel.getValue() == null
                || fieldModel.getValue() instanceof String value && value.isBlank();
    }
}
