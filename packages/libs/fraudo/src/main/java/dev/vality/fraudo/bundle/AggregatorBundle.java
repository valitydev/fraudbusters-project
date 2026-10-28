package dev.vality.fraudo.bundle;

import dev.vality.fraudo.aggregator.UniqueValueAggregator;
import dev.vality.fraudo.payment.aggregator.CountPaymentAggregator;
import dev.vality.fraudo.payment.aggregator.SumPaymentAggregator;
import dev.vality.fraudo.payout.aggregator.CountPayoutAggregator;
import dev.vality.fraudo.payout.aggregator.SumPayoutAggregator;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AggregatorBundle<T, U> {

    private CountPaymentAggregator<T, U> countPaymentAggregator;
    private SumPaymentAggregator<T, U> sumPaymentAggregator;
    private UniqueValueAggregator<T, U> uniqueValueAggregator;
    private CountPayoutAggregator<T, U> countPayoutAggregator;
    private SumPayoutAggregator<T, U> sumPayoutAggregator;
    private UniqueValueAggregator<T, U> uniquePayoutValueAggregator;

}
