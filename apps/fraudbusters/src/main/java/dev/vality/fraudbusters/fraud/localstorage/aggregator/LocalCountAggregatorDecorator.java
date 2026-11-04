package dev.vality.fraudbusters.fraud.localstorage.aggregator;

import dev.vality.fraudbusters.domain.TimeBound;
import dev.vality.fraudbusters.exception.RuleFunctionException;
import dev.vality.fraudbusters.fraud.AggregateGroupingFunction;
import dev.vality.fraudbusters.fraud.constant.CheckedField;
import dev.vality.fraudbusters.fraud.localstorage.LocalPaymentFieldResolver;
import dev.vality.fraudbusters.fraud.localstorage.LocalResultStorageRepository;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.CountAggregatorImpl;
import dev.vality.fraudbusters.service.TimeBoundaryService;
import dev.vality.fraudbusters.util.TimestampUtil;
import dev.vality.fraudo.model.TimeWindow;
import dev.vality.fraudo.payment.aggregator.CountPaymentAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class LocalCountAggregatorDecorator implements CountPaymentAggregator<PaymentModel, CheckedField> {

    private final CountAggregatorImpl<PaymentModel> countAggregator;
    private final LocalPaymentFieldResolver localPaymentFieldResolver;
    private final LocalResultStorageRepository localStorageRepository;
    private final TimeBoundaryService timeBoundaryService;

    @Override
    public Integer count(
            CheckedField checkedField,
            PaymentModel paymentModel,
            TimeWindow timeWindow,
            List<CheckedField> list) {
        Integer count = countAggregator.count(checkedField, paymentModel, timeWindow, list);
        FieldModel resolve = localPaymentFieldResolver.resolve(checkedField, paymentModel);
        Instant timestamp = TimestampUtil.instantFromModel(paymentModel);
        TimeBound timeBound = timeBoundaryService.getBoundary(timestamp, timeWindow);
        Integer localCount = localStorageRepository.countOperationByField(
                resolve.getName(),
                resolve.getValue(),
                timeBound.getLeft().getEpochSecond(),
                timeBound.getRight().getEpochSecond()
        );
        return localCount + count;
    }

    @Override
    public Integer countSuccess(
            CheckedField checkedField,
            PaymentModel paymentModel,
            TimeWindow timeWindow,
            List<CheckedField> list) {
        Integer countError = countAggregator.countSuccess(checkedField, paymentModel, timeWindow, list);
        Integer resultCount = getCount(
                checkedField,
                paymentModel,
                timeWindow,
                list,
                localStorageRepository::countOperationSuccessWithGroupBy
        ) + countError;
        log.debug("LocalStorageCountAggregatorImpl resultCount: {}", resultCount);
        return resultCount;
    }

    @Override
    public Integer countError(
            CheckedField checkedField, PaymentModel paymentModel, TimeWindow timeWindow,
            String errorCode, List<CheckedField> list) {
        try {
            Integer countError = countAggregator.countError(checkedField, paymentModel, timeWindow, errorCode, list);
            Instant timestamp = TimestampUtil.instantFromModel(paymentModel);
            TimeBound timeBound = timeBoundaryService.getBoundary(timestamp, timeWindow);
            FieldModel resolve = localPaymentFieldResolver.resolve(checkedField, paymentModel);
            List<FieldModel> eventFields = localPaymentFieldResolver.resolveListFields(paymentModel, list);
            Integer localCount = localStorageRepository.countOperationErrorWithGroupBy(
                    resolve.getName(),
                    resolve.getValue(),
                    timeBound.getLeft().getEpochSecond(),
                    timeBound.getRight().getEpochSecond(),
                    eventFields,
                    errorCode
            );
            int result = localCount + countError;
            log.debug(
                    "LocalStorageCountAggregatorImpl field: {} value: {}  countError: {}",
                    resolve.getName(),
                    resolve.getValue(),
                    result
            );
            return result;
        } catch (Exception e) {
            log.warn("LocalStorageCountAggregatorImpl error when countError e: ", e);
            throw new RuleFunctionException(e);
        }
    }

    @Override
    public Integer countError(CheckedField paymentCheckedField, PaymentModel paymentModel, TimeWindow timeWindow,
                              List<CheckedField> list) {
        try {
            Integer countError = countAggregator.countError(paymentCheckedField, paymentModel, timeWindow, list);
            Instant timestamp = TimestampUtil.instantFromModel(paymentModel);
            TimeBound timeBound = timeBoundaryService.getBoundary(timestamp, timeWindow);
            FieldModel resolve = localPaymentFieldResolver.resolve(paymentCheckedField, paymentModel);
            List<FieldModel> eventFields = localPaymentFieldResolver.resolveListFields(paymentModel, list);
            Integer localCount = localStorageRepository.countOperationErrorWithGroupBy(
                    resolve.getName(),
                    resolve.getValue(),
                    timeBound.getLeft().getEpochSecond(),
                    timeBound.getRight().getEpochSecond(),
                    eventFields
            );
            int result = localCount + countError;
            log.debug(
                    "LocalStorageCountAggregatorImpl field: {} value: {}  countError: {}",
                    resolve.getName(),
                    resolve.getValue(),
                    result
            );
            return result;
        } catch (Exception e) {
            log.warn("LocalStorageCountAggregatorImpl error when countError e: ", e);
            throw new RuleFunctionException(e);
        }
    }

    @Override
    public Integer countPending(CheckedField checkedField, PaymentModel paymentModel, TimeWindow timeWindow,
                                List<CheckedField> list) {
        Integer countFinished = countAggregator.count(checkedField, paymentModel, timeWindow, list);
        Integer resultCount = getCount(
                checkedField,
                paymentModel,
                timeWindow,
                list,
                localStorageRepository::countOperationPendingWithGroupBy
        ) - countFinished;
        log.debug("LocalStorageCountAggregatorImpl resultCount: {}", resultCount);
        return resultCount;
    }

    @Override
    public Integer countChargeback(
            CheckedField checkedField,
            PaymentModel model,
            TimeWindow timeWindow,
            List<CheckedField> fields) {
        return countAggregator.countChargeback(checkedField, model, timeWindow, fields);
    }

    @Override
    public Integer countRefund(
            CheckedField checkedField,
            PaymentModel model,
            TimeWindow timeWindow,
            List<CheckedField> fields) {
        return countAggregator.countRefund(checkedField, model, timeWindow, fields);
    }

    @NotNull
    private Integer getCount(
            CheckedField checkedField,
            PaymentModel paymentModel,
            TimeWindow timeWindow,
            List<CheckedField> list,
            AggregateGroupingFunction<String, Object, Long, Long, List<FieldModel>, Integer> aggregateFunction) {
        try {
            Instant timestamp = TimestampUtil.instantFromModel(paymentModel);
            TimeBound timeBound = timeBoundaryService.getBoundary(timestamp, timeWindow);
            FieldModel resolve = localPaymentFieldResolver.resolve(checkedField, paymentModel);
            List<FieldModel> eventFields = localPaymentFieldResolver.resolveListFields(paymentModel, list);
            Integer count = aggregateFunction.accept(
                    resolve.getName(),
                    resolve.getValue(),
                    timeBound.getLeft().getEpochSecond(),
                    timeBound.getRight().getEpochSecond(),
                    eventFields
            );
            log.debug(
                    "LocalStorageCountAggregatorImpl field: {} value: {}  count: {}",
                    resolve.getName(),
                    resolve.getValue(),
                    count
            );
            return count;
        } catch (Exception e) {
            log.warn("LocalStorageCountAggregatorImpl error when getCount e: ", e);
            throw new RuleFunctionException(e);
        }
    }
}
