package dev.vality.fraudbusters.stream.impl;

import dev.vality.fraudbusters.constant.TemplateLevel;
import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudbusters.stream.TemplateVisitor;
import dev.vality.fraudbusters.util.ReferenceKeyGenerator;
import dev.vality.fraudo.constant.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/** Payment visitor stays the default for consumers that inject the visitor by type. */
@Slf4j
@Primary
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
        log.debug("TemplateVisitorImpl visit paymentModel: {}", model);
        String partyId = model.getPartyId();
        String partyShopKey = ReferenceKeyGenerator.generateTemplateKey(partyId, model.getShopId());
        return cascade.apply(List.of(
                cascade.template(model, TemplateLevel.GLOBAL.name()),
                cascade.group(model, partyId),
                cascade.group(model, partyShopKey),
                cascade.template(model, partyId),
                cascade.template(model, partyShopKey)
        ), ResultStatus.THREE_DS);
    }
}
