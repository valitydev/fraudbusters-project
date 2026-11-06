package dev.vality.fraudbusters.stream.impl;

import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.domain.ConcreteResultModel;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudbusters.stream.RuleApplier;
import dev.vality.fraudbusters.util.CheckedResultModelUtil;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.BaseModel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/** Applies rules of one operation type; the caller defines the order of template and group references. */
@RequiredArgsConstructor
public class RuleCascade<T extends BaseModel> {

    private static final String RULE_NOT_CHECKED = "RULE_NOT_CHECKED";

    private final RuleApplier<T> ruleApplier;
    private final Pool<List<String>> groups;
    private final Pool<String> references;
    private final Pool<String> groupReferences;

    public Supplier<Optional<CheckedResultModel>> applyTemplate(T model, String referenceKey) {
        return () -> ruleApplier.apply(model, references.get(referenceKey));
    }

    public Supplier<Optional<CheckedResultModel>> applyGroup(T model, String groupReferenceKey) {
        return () -> ruleApplier.applyForAny(model, groups.get(groupReferences.get(groupReferenceKey)));
    }

    public CheckedResultModel apply(List<Supplier<Optional<CheckedResultModel>>> steps, ResultStatus defaultStatus) {
        return CheckedResultModelUtil.findFirstDecision(steps)
                .orElseGet(() -> createDefaultResult(defaultStatus));
    }

    private CheckedResultModel createDefaultResult(ResultStatus status) {
        ConcreteResultModel result = new ConcreteResultModel();
        result.setResultStatus(status);
        CheckedResultModel checked = new CheckedResultModel();
        checked.setResultModel(result);
        checked.setCheckedTemplate(RULE_NOT_CHECKED);
        return checked;
    }
}
