package dev.vality.fraudbusters.fraud.payout;

import dev.vality.fraudbusters.exception.RuleFunctionException;
import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.OperationModel;
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
public class PayoutAggregators<T extends OperationModel> implements CountPayoutAggregator<T, PaymentCheckedField>,
        SumPayoutAggregator<T, PaymentCheckedField>,
        UniqueValueAggregator<T, PaymentCheckedField> {

    private final PayoutHistoryRepository repository;
    private final TimeBoundaryService timeBoundaryService;

    @Override
    public Integer count(PaymentCheckedField field, T model, TimeWindow window,
                         List<PaymentCheckedField> groups) {
        return (int) aggregate("count()", field, model, window, groups, null, null);
    }

    @Override
    public Integer countSuccess(PaymentCheckedField field, T model, TimeWindow window,
                                List<PaymentCheckedField> groups) {
        return (int) aggregate("count()", field, model, window, groups, "succeeded", null);
    }

    @Override
    public Integer countError(PaymentCheckedField field, T model, TimeWindow window,
                              List<PaymentCheckedField> groups) {
        return countError(field, model, window, null, groups);
    }

    @Override
    public Integer countError(PaymentCheckedField field, T model, TimeWindow window,
                              String errorCode, List<PaymentCheckedField> groups) {
        return (int) aggregate("count()", field, model, window, groups, "failed", errorCode);
    }

    @Override
    public Double sum(PaymentCheckedField field, T model, TimeWindow window,
                      List<PaymentCheckedField> groups) {
        return aggregate("sum(amount)", field, model, window, groups, null, null);
    }

    @Override
    public Double sumSuccess(PaymentCheckedField field, T model, TimeWindow window,
                             List<PaymentCheckedField> groups) {
        return aggregate("sum(amount)", field, model, window, groups, "succeeded", null);
    }

    @Override
    public Double sumError(PaymentCheckedField field, T model, TimeWindow window,
                           List<PaymentCheckedField> groups) {
        return sumError(field, model, window, null, groups);
    }

    @Override
    public Double sumError(PaymentCheckedField field, T model, TimeWindow window,
                           String errorCode, List<PaymentCheckedField> groups) {
        return aggregate("sum(amount)", field, model, window, groups, "failed", errorCode);
    }

    @Override
    public Integer countUniqueValue(PaymentCheckedField field, T model, PaymentCheckedField uniqueField,
                                    TimeWindow window, List<PaymentCheckedField> groups) {
        return (int) aggregate("uniqExact(" + PayoutFields.column(uniqueField) + ")", field, model, window, groups,
                null, null);
    }

    private double aggregate(String expression, PaymentCheckedField field, T model, TimeWindow window,
                             List<PaymentCheckedField> groups, String status, String errorCode) {
        try {
            List<FieldModel> filters = new ArrayList<>();
            filters.add(PayoutFields.resolve(field, model));
            if (groups != null) {
                groups.forEach(group -> filters.add(PayoutFields.resolve(group, model)));
            }
            if (filters.stream().anyMatch(filter -> filter.getValue() == null
                    || filter.getValue() instanceof String value && value.isBlank())) {
                return 0;
            }
            var bound = timeBoundaryService.getBoundary(TimestampUtil.instantFromPaymentModel(model), window);
            return repository.aggregate(expression, filters, bound, status, errorCode);
        } catch (Exception e) {
            throw new RuleFunctionException(e);
        }
    }
}
