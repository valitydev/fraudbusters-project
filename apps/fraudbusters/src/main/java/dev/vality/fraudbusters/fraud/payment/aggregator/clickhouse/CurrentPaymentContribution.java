package dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse;

import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.fraudbusters.fraud.model.PaymentModel;

final class CurrentPaymentContribution {

    private CurrentPaymentContribution() {
    }

    static int count(PaymentModel model) {
        return model.getOperationType() == OperationType.payment ? 1 : 0;
    }

    static long amount(PaymentModel model) {
        return count(model) == 0 || model.getAmount() == null ? 0 : model.getAmount();
    }
}
