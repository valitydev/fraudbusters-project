package dev.vality.fraudbusters.mg.connector.constant;

import dev.vality.geck.filter.Condition;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;

@SuppressWarnings("LineLength")
public enum InvoiceEventType implements EventType {

    INVOICE_PAYMENT_STATUS_CHANGED(
            "invoice_payment_change.payload.invoice_payment_status_changed",
            new IsNullCondition().not()),

    INVOICE_PAYMENT_STARTED(
            "invoice_payment_change.payload.invoice_payment_started",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_REFUND_STATUS_CHANGED(
            "invoice_payment_change.payload.invoice_payment_refund_change.payload.invoice_payment_refund_status_changed",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_CHARGEBACK_STATUS_CHANGED(
            "invoice_payment_change.payload.invoice_payment_chargeback_change.payload.invoice_payment_chargeback_status_changed",
            new IsNullCondition().not());

    Filter filter;

    InvoiceEventType(String path, Condition... conditions) {
        this.filter = new PathConditionFilter(new PathConditionRule(path, conditions));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
