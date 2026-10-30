package dev.vality.fraudo.payout.aggregator;

import dev.vality.fraudo.aggregator.CountAggregator;
import dev.vality.fraudo.model.TimeWindow;

import java.util.List;

/**
 * Reads completed withdrawals (succeeded and failed), independently of the inspected operation type.
 * Implementations must deduplicate history by operation ID and must not count an unconfirmed current operation.
 */
public interface CountPayoutAggregator<T, U> extends CountAggregator<T, U> {

    Integer countSuccess(U checkedField, T model, TimeWindow timeWindow, List<U> fields);

    Integer countError(U checkedField, T model, TimeWindow timeWindow, List<U> fields);

    Integer countError(U checkedField, T model, TimeWindow timeWindow, String errorCode, List<U> fields);

}
