package dev.vality.fraudo.payment.aggregator;

import dev.vality.fraudo.aggregator.CountAggregator;
import dev.vality.fraudo.model.TimeWindow;

import java.util.List;

public interface CountPaymentAggregator<T, U> extends CountAggregator<T, U> {

    Integer countSuccess(U checkedField, T model, TimeWindow timeWindow, List<U> fields);

    Integer countError(U checkedField, T model, TimeWindow timeWindow, String errorCode, List<U> fields);

    Integer countError(U checkedField, T model, TimeWindow timeWindow, List<U> fields);

    Integer countPending(U checkedField, T model, TimeWindow timeWindow, List<U> fields);

    Integer countChargeback(U checkedField, T model, TimeWindow timeWindow, List<U> fields);

    Integer countRefund(U checkedField, T model, TimeWindow timeWindow, List<U> fields);

}
