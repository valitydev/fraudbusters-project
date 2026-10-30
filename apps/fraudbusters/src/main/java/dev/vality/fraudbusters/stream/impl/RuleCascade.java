package dev.vality.fraudbusters.stream.impl;

import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.domain.ConcreteResultModel;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudo.constant.ResultStatus;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;
import dev.vality.fraudbusters.util.CheckedResultModelUtil;

/** Shared ordering for independently stored payment and payout rules. */
@RequiredArgsConstructor
public class RuleCascade {

    private final RuleApplierImpl<PaymentModel> ruleApplier;
    private final Pool<List<String>> groups;
    private final Pool<String> references;
    private final Pool<String> groupReferences;

    public CheckedResultModel apply(PaymentModel model, String globalKey, String partyKey, String subjectKey,
                                    ResultStatus defaultStatus) {
        return ruleApplier.apply(model, references.get(globalKey))
                .orElseGet(() -> ruleApplier.applyForAny(model, groups.get(groupReferences.get(partyKey)))
                        .orElseGet(() -> ruleApplier.applyForAny(model, groups.get(groupReferences.get(subjectKey)))
                                .orElseGet(() -> ruleApplier.apply(model, references.get(partyKey))
                                        .orElseGet(() -> ruleApplier.apply(model, references.get(subjectKey))
                                                .orElseGet(() -> defaultResult(defaultStatus))))));
    }

    public CheckedResultModel applyWithNotifications(PaymentModel model, String globalKey, String partyKey,
                                                     String subjectKey, ResultStatus defaultStatus) {
        List<Supplier<Optional<CheckedResultModel>>> steps = List.of(
                () -> ruleApplier.apply(model, references.get(globalKey)),
                () -> ruleApplier.applyForAny(model, groups.get(groupReferences.get(globalKey))),
                () -> ruleApplier.applyForAny(model, groups.get(groupReferences.get(partyKey))),
                () -> ruleApplier.applyForAny(model, groups.get(groupReferences.get(subjectKey))),
                () -> ruleApplier.apply(model, references.get(partyKey)),
                () -> ruleApplier.apply(model, references.get(subjectKey)));
        List<String> notifications = new ArrayList<>();
        CheckedResultModel notificationResult = null;
        for (var step : steps) {
            var result = step.get();
            if (result.isPresent()) {
                var checked = result.get();
                if (CheckedResultModelUtil.isTerminal(checked)) {
                    return CheckedResultModelUtil.finalizeCheckedResultModel(checked, notifications);
                }
                notifications.addAll(checked.getResultModel().getNotificationsRule());
                notificationResult = checked;
            }
        }
        if (notificationResult != null) {
            notificationResult.getResultModel().setNotificationsRule(notifications);
            return notificationResult;
        }
        return defaultResult(defaultStatus);
    }

    private CheckedResultModel defaultResult(ResultStatus status) {
        ConcreteResultModel result = new ConcreteResultModel();
        result.setResultStatus(status);
        CheckedResultModel checked = new CheckedResultModel();
        checked.setResultModel(result);
        checked.setCheckedTemplate("RULE_NOT_CHECKED");
        return checked;
    }
}
