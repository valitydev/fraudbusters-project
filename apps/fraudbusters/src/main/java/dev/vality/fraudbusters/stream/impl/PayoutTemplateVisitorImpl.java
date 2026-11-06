package dev.vality.fraudbusters.stream.impl;

import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.util.PayoutScope;
import dev.vality.fraudbusters.stream.TemplateVisitor;
import dev.vality.fraudo.constant.ResultStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PayoutTemplateVisitorImpl implements TemplateVisitor<PayoutModel, CheckedResultModel> {

    private final RuleCascade<PayoutModel> payoutRuleCascade;

    @Override
    public CheckedResultModel visit(PayoutModel model) {
        if (!StringUtils.hasText(model.getPartyId()) || !StringUtils.hasText(model.getWalletId())) {
            throw new IllegalArgumentException("party_id and wallet_id are required for withdrawal inspection");
        }
        String partyKey = PayoutScope.generateKey(model.getPartyId(), null);
        String walletKey = PayoutScope.generateKey(model.getPartyId(), model.getWalletId());
        return payoutRuleCascade.apply(List.of(
                payoutRuleCascade.applyTemplate(model, PayoutScope.GLOBAL),
                payoutRuleCascade.applyGroup(model, PayoutScope.GLOBAL),
                payoutRuleCascade.applyGroup(model, partyKey),
                payoutRuleCascade.applyGroup(model, walletKey),
                payoutRuleCascade.applyTemplate(model, partyKey),
                payoutRuleCascade.applyTemplate(model, walletKey)
        ), ResultStatus.HIGH_RISK);
    }
}
