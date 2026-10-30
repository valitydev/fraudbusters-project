package dev.vality.fraudbusters.stream.impl;

import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudbusters.stream.RuleApplier;
import dev.vality.fraudbusters.util.CheckedResultFactory;
import dev.vality.fraudo.model.BaseModel;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudbusters.util.CheckedResultModelUtil;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.visitor.TemplateVisitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class RuleApplierImpl<T extends BaseModel> implements RuleApplier<T> {

    private final TemplateVisitor<T, ResultModel> templateVisitor;

    private final Pool<ParserRuleContext> templatePool;
    private final CheckedResultFactory checkedResultFactory;
    private final boolean includeNotifications;

    public RuleApplierImpl(TemplateVisitor<T, ResultModel> visitor, Pool<ParserRuleContext> templates,
                           CheckedResultFactory results) {
        this(visitor, templates, results, false);
    }

    @Override
    public Optional<CheckedResultModel> apply(T model, String templateKey) {
        ParserRuleContext parseContext = templatePool.get(templateKey);
        if (parseContext != null) {
            ResultModel resultModel = templateVisitor.visit(parseContext, model);
            if (includeNotifications) {
                var result = checkedResultFactory.createCheckedResultWithNotifications(templateKey, resultModel);
                return result.getResultModel().getResultStatus() == ResultStatus.NORMAL
                        ? Optional.empty() : Optional.of(result);
            }
            return checkedResultFactory.createCheckedResult(templateKey, resultModel);
        }
        return Optional.empty();
    }

    @Override
    public Optional<CheckedResultModel> applyForAny(T model, List<String> templateKeys) {
        List<String> notifications = new ArrayList<>();
        CheckedResultModel notificationResult = null;
        if (templateKeys != null) {
            for (String templateKey : templateKeys) {
                Optional<CheckedResultModel> result = apply(model, templateKey);
                if (result.isPresent()) {
                    if (includeNotifications && result.get().getResultModel().getResultStatus() ==
                            ResultStatus.NOTIFY) {
                        notificationResult = result.get();
                        notifications.addAll(result.get().getResultModel().getNotificationsRule());
                    } else {
                        return Optional.of(CheckedResultModelUtil
                                .finalizeCheckedResultModel(result.get(), notifications));
                    }
                }
            }
        }
        if (notificationResult != null) {
            notificationResult.getResultModel().setNotificationsRule(notifications);
        }
        return Optional.ofNullable(notificationResult);
    }

}
