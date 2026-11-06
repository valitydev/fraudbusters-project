package dev.vality.fraudbusters.stream.impl;

import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudbusters.stream.RuleApplier;
import dev.vality.fraudbusters.util.CheckedResultFactory;
import dev.vality.fraudbusters.util.CheckedResultModelUtil;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.BaseModel;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.visitor.TemplateVisitor;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Unlike {@link RuleApplierImpl}, returns notification-only results so that later rules still make the decision.
 */
@RequiredArgsConstructor
public class NotifyingRuleApplierImpl<T extends BaseModel> implements RuleApplier<T> {

    private final TemplateVisitor<T, ResultModel> templateVisitor;
    private final Pool<ParserRuleContext> templatePool;
    private final CheckedResultFactory checkedResultFactory;

    @Override
    public Optional<CheckedResultModel> apply(T model, String templateKey) {
        ParserRuleContext parseContext = templatePool.get(templateKey);
        if (parseContext == null) {
            return Optional.empty();
        }
        ResultModel resultModel = templateVisitor.visit(parseContext, model);
        CheckedResultModel result = checkedResultFactory.createCheckedResultWithNotifications(templateKey, resultModel);
        return result.getResultModel().getResultStatus() == ResultStatus.NORMAL
                ? Optional.empty()
                : Optional.of(result);
    }

    @Override
    public Optional<CheckedResultModel> applyForAny(T model, List<String> templateKeys) {
        if (templateKeys == null) {
            return Optional.empty();
        }
        return CheckedResultModelUtil.findFirstDecision(templateKeys.stream()
                .<Supplier<Optional<CheckedResultModel>>>map(templateKey -> () -> apply(model, templateKey))
                .toList());
    }

}
