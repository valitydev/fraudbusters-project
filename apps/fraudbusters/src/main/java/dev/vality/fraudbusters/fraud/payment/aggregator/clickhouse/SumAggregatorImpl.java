package dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse;

import dev.vality.fraudbusters.aspect.BasicMetric;
import dev.vality.fraudbusters.domain.TimeBound;
import dev.vality.fraudbusters.exception.RuleFunctionException;
import dev.vality.fraudbusters.fraud.AggregateGroupingFunction;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.OperationModel;
import dev.vality.fraudbusters.fraud.resolver.HistoryFieldResolver;
import dev.vality.fraudbusters.repository.AggregationRepository;
import dev.vality.fraudbusters.repository.PaymentRepository;
import dev.vality.fraudbusters.service.TimeBoundaryService;
import dev.vality.fraudbusters.util.TimestampUtil;
import dev.vality.fraudo.model.TimeWindow;
import dev.vality.fraudo.payment.aggregator.SumPaymentAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class SumAggregatorImpl<T extends OperationModel, U> implements SumPaymentAggregator<T, U> {

    private final HistoryFieldResolver<T, U> databasePaymentFieldResolver;
    private final PaymentRepository paymentRepository;
    private final AggregationRepository refundRepository;
    private final AggregationRepository chargebackRepository;
    private final TimeBoundaryService timeBoundaryService;

    @Override
    @BasicMetric("sum")
    public Double sum(
            U checkedField,
            T model,
            TimeWindow timeWindow,
            List<U> list) {
        return getSum(checkedField, model, timeWindow, list, paymentRepository::sumOperationByFieldWithGroupBy);
    }

    @Override
    @BasicMetric("sumSuccess")
    public Double sumSuccess(
            U checkedField,
            T model,
            TimeWindow timeWindow,
            List<U> list) {
        return getSum(checkedField, model, timeWindow, list, paymentRepository::sumOperationSuccessWithGroupBy,
                false);
    }

    @Override
    @BasicMetric("sumError")
    public Double sumError(
            U checkedField, T model, TimeWindow timeWindow, String errorCode,
            List<U> list) {
        try {
            Instant timestamp = TimestampUtil.instantFromModel(model);
            TimeBound timeBound = timeBoundaryService.getBoundary(timestamp, timeWindow);
            FieldModel resolve = databasePaymentFieldResolver.resolve(checkedField, model);
            if (Objects.isNull(resolve.getValue())) {
                return Double.valueOf(CurrentPaymentContribution.getAmount(model));
            }
            List<FieldModel> eventFields = databasePaymentFieldResolver.resolveListFields(model, list);
            Long sum = paymentRepository.sumOperationErrorWithGroupBy(
                    resolve.getName(),
                    resolve.getValue(),
                    timeBound.getLeft().toEpochMilli(),
                    timeBound.getRight().toEpochMilli(),
                    eventFields,
                    errorCode
            );
            double resultSum = (double) checkedLong(sum);
            log.debug(
                    "SumAggregatorImpl field: {} value: {}  sumError: {}",
                    resolve.getName(),
                    resolve.getValue(),
                    resultSum
            );
            return resultSum;
        } catch (Exception e) {
            log.warn("SumAggregatorImpl error when sumError e: ", e);
            throw new RuleFunctionException(e);
        }
    }

    @Override
    public Double sumError(U checkedField,
                           T model,
                           TimeWindow timeWindow,
                           List<U> list) {
        try {
            Instant timestamp = TimestampUtil.instantFromModel(model);
            TimeBound timeBound = timeBoundaryService.getBoundary(timestamp, timeWindow);
            FieldModel resolve = databasePaymentFieldResolver.resolve(checkedField, model);
            if (Objects.isNull(resolve.getValue())) {
                return Double.valueOf(CurrentPaymentContribution.getAmount(model));
            }
            List<FieldModel> eventFields = databasePaymentFieldResolver.resolveListFields(model, list);
            Long sum = paymentRepository.sumOperationErrorWithGroupBy(
                    resolve.getName(),
                    resolve.getValue(),
                    timeBound.getLeft().toEpochMilli(),
                    timeBound.getRight().toEpochMilli(),
                    eventFields
            );
            double resultSum = (double) checkedLong(sum) + CurrentPaymentContribution.getAmount(model);
            log.debug(
                    "SumAggregatorImpl field: {} value: {}  sumError: {}",
                    resolve.getName(),
                    resolve.getValue(),
                    resultSum
            );
            return resultSum;
        } catch (Exception e) {
            log.warn("SumAggregatorImpl error when sumError e: ", e);
            throw new RuleFunctionException(e);
        }
    }

    @Override
    public Double sumChargeback(
            U checkedField,
            T model,
            TimeWindow timeWindow,
            List<U> list) {
        return getSum(
                checkedField,
                model,
                timeWindow,
                list,
                chargebackRepository::sumOperationByFieldWithGroupBy,
                false
        );
    }

    @Override
    public Double sumRefund(
            U checkedField,
            T model,
            TimeWindow timeWindow,
            List<U> list) {
        return getSum(
                checkedField,
                model,
                timeWindow,
                list,
                refundRepository::sumOperationByFieldWithGroupBy,
                false
        );
    }

    @NotNull
    private Double getSum(
            U checkedField,
            T model,
            TimeWindow timeWindow,
            List<U> list,
            AggregateGroupingFunction<String, Object, Long, Long, List<FieldModel>, Long> aggregateFunction) {
        return getSum(checkedField, model, timeWindow, list, aggregateFunction, true);
    }

    @NotNull
    @BasicMetric("getSumWindowed")
    private Double getSum(
            U checkedField,
            T model,
            TimeWindow timeWindow,
            List<U> list,
            AggregateGroupingFunction<String, Object, Long, Long, List<FieldModel>, Long> aggregateFunction,
            boolean withCurrent) {
        try {
            Instant timestamp = TimestampUtil.instantFromModel(model);
            TimeBound timeBound = timeBoundaryService.getBoundary(timestamp, timeWindow);
            FieldModel resolve = databasePaymentFieldResolver.resolve(checkedField, model);
            if (Objects.isNull(resolve.getValue())) {
                return Double.valueOf(CurrentPaymentContribution.getAmount(model));
            }
            List<FieldModel> eventFields = databasePaymentFieldResolver.resolveListFields(model, list);
            Long sum = aggregateFunction.accept(
                    resolve.getName(),
                    resolve.getValue(),
                    timeBound.getLeft().toEpochMilli(),
                    timeBound.getRight().toEpochMilli(),
                    eventFields
            );
            double resultSum =
                    withCurrent ? (double) checkedLong(sum) + CurrentPaymentContribution.getAmount(model)
                            : checkedLong(sum);
            log.debug(
                    "SumAggregatorImpl field: {} value: {}  sum: {}",
                    resolve.getName(),
                    resolve.getValue(),
                    resultSum
            );
            return resultSum;
        } catch (Exception e) {
            log.warn("SumAggregatorImpl error when getSum e: ", e);
            throw new RuleFunctionException(e);
        }
    }

    private Long checkedLong(Long entry) {
        return entry != null ? entry : 0L;
    }
}
