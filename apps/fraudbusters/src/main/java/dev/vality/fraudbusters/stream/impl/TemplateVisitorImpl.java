package dev.vality.fraudbusters.stream.impl;

import dev.vality.fraudbusters.constant.TemplateLevel;
import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudbusters.stream.TemplateVisitor;
import dev.vality.fraudbusters.util.ReferenceKeyGenerator;
import dev.vality.fraudo.constant.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TemplateVisitorImpl implements TemplateVisitor<PaymentModel, CheckedResultModel> {

    private final RuleCascade<PaymentModel> cascade;

    public TemplateVisitorImpl(RuleApplierImpl<PaymentModel> ruleApplier,
                               Pool<List<String>> groupPoolImpl,
                               Pool<String> referencePoolImpl,
                               Pool<String> groupReferencePoolImpl) {
        cascade = new RuleCascade<>(ruleApplier, groupPoolImpl, referencePoolImpl, groupReferencePoolImpl);
    }

    @Override
    public CheckedResultModel visit(PaymentModel model) {
        log.debug("TemplateVisitorImpl visit paymentModel: {}", model);
        String partyId = model.getPartyId();
        String partyShopKey = ReferenceKeyGenerator.generateTemplateKey(partyId, model.getShopId());
        return cascade.apply(List.of(
                cascade.applyTemplate(model, TemplateLevel.GLOBAL.name()),
                cascade.applyGroup(model, partyId),
                cascade.applyGroup(model, partyShopKey),
                cascade.applyTemplate(model, partyId),
                cascade.applyTemplate(model, partyShopKey)
        ), ResultStatus.THREE_DS);
    }
}
