package dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse;

import dev.vality.fraudbusters.fraud.model.OperationModel;
import dev.vality.fraudbusters.fraud.model.PaymentModel;

/** The inspected operation belongs to the payment history only when it is a payment itself. */
final class CurrentPaymentContribution {

    private CurrentPaymentContribution() {
    }

    static int count(OperationModel model) {
        return model instanceof PaymentModel ? 1 : 0;
    }

    static long amount(OperationModel model) {
        return count(model) == 0 || model.getAmount() == null ? 0 : model.getAmount();
    }
}
