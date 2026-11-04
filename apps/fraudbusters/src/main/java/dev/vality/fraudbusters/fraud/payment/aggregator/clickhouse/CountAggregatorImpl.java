package dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse;

import dev.vality.fraudbusters.aspect.BasicMetric;
import dev.vality.fraudbusters.domain.TimeBound;
import dev.vality.fraudbusters.exception.RuleFunctionException;
import dev.vality.fraudbusters.fraud.AggregateGroupingFunction;
import dev.vality.fraudbusters.fraud.constant.CheckedField;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.OperationModel;
import dev.vality.fraudbusters.fraud.payment.resolver.DatabasePaymentFieldResolver;
import dev.vality.fraudbusters.repository.AggregationRepository;
import dev.vality.fraudbusters.repository.PaymentRepository;
import dev.vality.fraudbusters.service.TimeBoundaryService;
import dev.vality.fraudbusters.util.TimestampUtil;
import dev.vality.fraudo.model.TimeWindow;
import dev.vality.fraudo.payment.aggregator.CountPaymentAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class CountAggregatorImpl<T extends OperationModel> implements CountPaymentAggregator<T, CheckedField> {

    private final DatabasePaymentFieldResolver databasePaymentFieldResolver;
    private final PaymentRepository paymentRepository;
    private final AggregationRepository refundRepository;
    private final AggregationRepository chargebackRepository;
    private final TimeBoundaryService timeBoundaryService;

    @Override
    @BasicMetric("count")
    public Integer count(
            CheckedField checkedField,
            T paymentModel,
            TimeWindow timeWindow,
            List<CheckedField> list) {
        return getCount(
                checkedField,
                paymentModel,
                timeWindow,
                list,
                paymentRepository::countOperationByFieldWithGroupBy
        );
    }

    @Override
    @BasicMetric("countSuccess")
    public Integer countSuccess(
            CheckedField checkedField,
            T paymentModel,
            TimeWindow timeWindow,
            List<CheckedField> list) {
        return getCount(
                checkedField,
                paymentModel,
                timeWindow,
                list,
                paymentRepository::countOperationSuccessWithGroupBy,
                false
        );
    }

    @Override
    @BasicMetric("countError")
    public Integer countError(
            CheckedField checkedField, T paymentModel, TimeWindow timeWindow,
            String errorCode, List<CheckedField> list) {
        try {
            Instant timestamp = TimestampUtil.instantFromModel(paymentModel);
            TimeBound timeBound = timeBoundaryService.getBoundary(timestamp, timeWindow);
            FieldModel resolve = databasePaymentFieldResolver.resolve(checkedField, paymentModel);
            List<FieldModel> eventFields = databasePaymentFieldResolver.resolveListFields(paymentModel, list);
            if (Objects.isNull(resolve.getValue())) {
                return 0;
            }
            Integer count = paymentRepository.countOperationErrorWithGroupBy(
                    resolve.getName(),
                    resolve.getValue(),
                    timeBound.getLeft().toEpochMilli(),
                    timeBound.getRight().toEpochMilli(),
                    eventFields,
                    errorCode
            );

            log.debug(
                    "CountAggregatorImpl field: {} value: {}  countError: {}",
                    resolve.getName(),
                    resolve.getValue(),
                    count
            );
            return count;
        } catch (Exception e) {
            log.warn("CountAggregatorImpl error when countError e: ", e);
            throw new RuleFunctionException(e);
        }
    }

    @Override
    public Integer countError(CheckedField checkedField,
                              T paymentModel,
                              TimeWindow timeWindow,
                              List<CheckedField> list) {
        try {
            Instant timestamp = TimestampUtil.instantFromModel(paymentModel);
            TimeBound timeBound = timeBoundaryService.getBoundary(timestamp, timeWindow);
            FieldModel resolve = databasePaymentFieldResolver.resolve(checkedField, paymentModel);
            List<FieldModel> eventFields = databasePaymentFieldResolver.resolveListFields(paymentModel, list);
            if (Objects.isNull(resolve.getValue())) {
                return 0;
            }
            Integer count = paymentRepository.countOperationErrorWithGroupBy(
                    resolve.getName(),
                    resolve.getValue(),
                    timeBound.getLeft().toEpochMilli(),
                    timeBound.getRight().toEpochMilli(),
                    eventFields
            );

            log.debug(
                    "CountAggregatorImpl field: {} value: {}  countError: {}",
                    resolve.getName(),
                    resolve.getValue(),
                    count
            );
            return count + CurrentPaymentContribution.count(paymentModel);
        } catch (Exception e) {
            log.warn("CountAggregatorImpl error when countError e: ", e);
            throw new RuleFunctionException(e);
        }
    }

    @Override
    public Integer countPending(CheckedField checkedField, T paymentModel, TimeWindow timeWindow,
                                List<CheckedField> list) {
        Integer countFinished = count(checkedField, paymentModel, timeWindow, list);
        return getCount(
                checkedField,
                paymentModel,
                timeWindow,
                list,
                paymentRepository::countOperationPendingWithGroupBy
        ) - countFinished;
    }

    @Override
    @BasicMetric("countChargeback")
    public Integer countChargeback(
            CheckedField paymentCheckedField,
            T paymentModel,
            TimeWindow timeWindow,
            List<CheckedField> list) {
        return getCount(
                paymentCheckedField,
                paymentModel,
                timeWindow,
                list,
                chargebackRepository::countOperationByFieldWithGroupBy,
                false
        );
    }

    @Override
    @BasicMetric("countRefund")
    public Integer countRefund(
            CheckedField paymentCheckedField,
            T paymentModel,
            TimeWindow timeWindow,
            List<CheckedField> list) {
        return getCount(
                paymentCheckedField,
                paymentModel,
                timeWindow,
                list,
                refundRepository::countOperationByFieldWithGroupBy,
                false
        );
    }

    @NotNull
    private Integer getCount(
            CheckedField checkedField,
            T paymentModel,
            TimeWindow timeWindow,
            List<CheckedField> list,
            AggregateGroupingFunction<String, Object, Long, Long, List<FieldModel>, Integer> aggregateFunction) {
        return getCount(checkedField, paymentModel, timeWindow, list, aggregateFunction, true);
    }

    @NotNull
    private Integer getCount(
            CheckedField checkedField,
            T paymentModel,
            TimeWindow timeWindow,
            List<CheckedField> list,
            AggregateGroupingFunction<String, Object, Long, Long, List<FieldModel>, Integer> aggregateFunction,
            boolean withCurrent) {
        try {
            Instant timestamp = TimestampUtil.instantFromModel(paymentModel);
            TimeBound timeBound = timeBoundaryService.getBoundary(timestamp, timeWindow);
            FieldModel resolve = databasePaymentFieldResolver.resolve(checkedField, paymentModel);
            List<FieldModel> eventFields = databasePaymentFieldResolver.resolveListFields(paymentModel, list);
            if (Objects.isNull(resolve.getValue())) {
                return withCurrent ? CurrentPaymentContribution.count(paymentModel) : 0;
            }
            Integer count = aggregateFunction.accept(
                    resolve.getName(),
                    resolve.getValue(),
                    timeBound.getLeft().toEpochMilli(),
                    timeBound.getRight().toEpochMilli(),
                    eventFields
            );
            log.debug(
                    "CountAggregatorImpl field: {} value: {}  count: {}",
                    resolve.getName(),
                    resolve.getValue(),
                    count
            );
            return withCurrent ? count + CurrentPaymentContribution.count(paymentModel) : count;
        } catch (Exception e) {
            log.warn("CountAggregatorImpl error when getCount e: ", e);
            throw new RuleFunctionException(e);
        }
    }
}
