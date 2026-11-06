package dev.vality.fraudbusters.stream.impl;

import dev.vality.fraudbusters.constant.TemplateLevel;
import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.stream.TemplateVisitor;
import dev.vality.fraudbusters.util.ReferenceKeyGenerator;
import dev.vality.fraudo.constant.ResultStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateVisitorImpl implements TemplateVisitor<PaymentModel, CheckedResultModel> {

    private final RuleCascade<PaymentModel> paymentRuleCascade;

    @Override
    public CheckedResultModel visit(PaymentModel model) {
        log.debug("TemplateVisitorImpl visit paymentModel: {}", model);
        String partyId = model.getPartyId();
        String partyShopKey = ReferenceKeyGenerator.generateTemplateKey(partyId, model.getShopId());
        return paymentRuleCascade.apply(List.of(
                paymentRuleCascade.applyTemplate(model, TemplateLevel.GLOBAL.name()),
                paymentRuleCascade.applyGroup(model, partyId),
                paymentRuleCascade.applyGroup(model, partyShopKey),
                paymentRuleCascade.applyTemplate(model, partyId),
                paymentRuleCascade.applyTemplate(model, partyShopKey)
        ), ResultStatus.THREE_DS);
    }
}
