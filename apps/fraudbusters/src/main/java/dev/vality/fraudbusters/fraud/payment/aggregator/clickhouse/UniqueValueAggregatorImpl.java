package dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse;

import dev.vality.fraudbusters.aspect.BasicMetric;
import dev.vality.fraudbusters.domain.TimeBound;
import dev.vality.fraudbusters.exception.RuleFunctionException;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.OperationModel;
import dev.vality.fraudbusters.fraud.resolver.HistoryFieldResolver;
import dev.vality.fraudbusters.repository.PaymentRepository;
import dev.vality.fraudbusters.service.TimeBoundaryService;
import dev.vality.fraudbusters.util.TimestampUtil;
import dev.vality.fraudo.aggregator.UniqueValueAggregator;
import dev.vality.fraudo.model.TimeWindow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class UniqueValueAggregatorImpl<T extends OperationModel, U>
        implements UniqueValueAggregator<T, U> {

    private static final int CURRENT_ONE = 1;

    private final HistoryFieldResolver<T, U> databasePaymentFieldResolver;
    private final PaymentRepository paymentRepository;
    private final TimeBoundaryService timeBoundaryService;
    /** Whether the inspected operation is itself part of the history this aggregator reads. */
    private final boolean includeCurrentOperation;

    @Override
    @BasicMetric("countUniqueValueWindowed")
    public Integer countUniqueValue(
            U countField,
            T model,
            U onField,
            TimeWindow timeWindow,
            List<U> list) {
        try {
            Instant timestamp = TimestampUtil.instantFromModel(model);
            TimeBound timeBound = timeBoundaryService.getBoundary(timestamp, timeWindow);
            FieldModel resolve = databasePaymentFieldResolver.resolve(countField, model);
            if (Objects.isNull(resolve.getValue())) {
                return getCurrentCount();
            }
            List<FieldModel> fieldModels = databasePaymentFieldResolver.resolveListFields(model, list);
            Integer uniqCountOperation = paymentRepository.uniqCountOperationWithGroupBy(
                    resolve.getName(),
                    resolve.getValue(),
                    databasePaymentFieldResolver.resolve(onField),
                    timeBound.getLeft().toEpochMilli(),
                    timeBound.getRight().toEpochMilli(),
                    fieldModels
            );
            return uniqCountOperation + getCurrentCount();
        } catch (Exception e) {
            log.warn("UniqueValueAggregatorImpl error when getCount e: ", e);
            throw new RuleFunctionException(e);
        }
    }

    private int getCurrentCount() {
        return includeCurrentOperation ? CURRENT_ONE : 0;
    }
}
