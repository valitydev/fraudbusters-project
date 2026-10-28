package dev.vality.fraudo.payout.aggregator;

import dev.vality.fraudo.aggregator.SumAggregator;
import dev.vality.fraudo.model.TimeWindow;

import java.util.List;

/**
 * Reads completed withdrawals (succeeded and failed), independently of the inspected operation type.
 * Implementations must deduplicate history by operation ID and must not count an unconfirmed current operation.
 * Sums use minor currency units without conversion; currency scope is supplied explicitly in fields.
 */
public interface SumPayoutAggregator<T, U> extends SumAggregator<T, U> {

    Double sumSuccess(U checkedField, T model, TimeWindow timeWindow, List<U> fields);

    Double sumError(U checkedField, T model, TimeWindow timeWindow, List<U> fields);

    Double sumError(U checkedField, T model, TimeWindow timeWindow, String errorCode, List<U> fields);

}
