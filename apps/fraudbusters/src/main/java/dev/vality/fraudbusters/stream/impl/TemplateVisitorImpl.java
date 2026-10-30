package dev.vality.fraudbusters.stream.impl;

import dev.vality.fraudbusters.constant.TemplateLevel;
import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudbusters.stream.TemplateVisitor;
import dev.vality.fraudbusters.util.ReferenceKeyGenerator;
import dev.vality.fraudo.constant.ResultStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TemplateVisitorImpl implements TemplateVisitor<PaymentModel, CheckedResultModel> {

    private final RuleCascade cascade;

    public TemplateVisitorImpl(RuleApplierImpl<PaymentModel> ruleApplier,
                               Pool<List<String>> groupPoolImpl,
                               Pool<String> referencePoolImpl,
                               Pool<String> groupReferencePoolImpl) {
        cascade = new RuleCascade(ruleApplier, groupPoolImpl, referencePoolImpl, groupReferencePoolImpl);
    }

    @Override
    public CheckedResultModel visit(PaymentModel model) {
        return cascade.apply(model, TemplateLevel.GLOBAL.name(), model.getPartyId(),
                ReferenceKeyGenerator.generateTemplateKey(model.getPartyId(), model.getShopId()),
                ResultStatus.THREE_DS);
    }
}
