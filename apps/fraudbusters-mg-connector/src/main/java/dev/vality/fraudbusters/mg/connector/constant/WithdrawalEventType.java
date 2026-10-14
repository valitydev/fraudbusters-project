package dev.vality.fraudbusters.mg.connector.constant;

import dev.vality.geck.filter.Condition;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;

public enum WithdrawalEventType implements EventType {

    WITHDRAWAL_PAYMENT_CHARGEBACK_STATUS_CHANGED("change.status_changed.status", new IsNullCondition().not());

    Filter filter;

    WithdrawalEventType(String path, Condition... conditions) {
        this.filter = new PathConditionFilter(new PathConditionRule(path, conditions));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
