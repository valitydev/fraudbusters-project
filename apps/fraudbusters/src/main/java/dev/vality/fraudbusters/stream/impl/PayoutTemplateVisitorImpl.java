package dev.vality.fraudbusters.stream.impl;

import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.fraud.payout.PayoutScope;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudbusters.stream.RuleApplier;
import dev.vality.fraudbusters.stream.TemplateVisitor;
import dev.vality.fraudo.constant.ResultStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class PayoutTemplateVisitorImpl implements TemplateVisitor<PayoutModel, CheckedResultModel> {

    private final RuleCascade<PayoutModel> cascade;

    public PayoutTemplateVisitorImpl(RuleApplier<PayoutModel> payoutRuleApplier,
                                     Pool<List<String>> payoutGroupPoolImpl,
                                     Pool<String> payoutReferencePoolImpl,
                                     Pool<String> payoutGroupReferencePoolImpl) {
        cascade = new RuleCascade<>(payoutRuleApplier, payoutGroupPoolImpl, payoutReferencePoolImpl,
                payoutGroupReferencePoolImpl);
    }

    @Override
    public CheckedResultModel visit(PayoutModel model) {
        if (!StringUtils.hasText(model.getPartyId()) || !StringUtils.hasText(model.getWalletId())) {
            throw new IllegalArgumentException("party_id and wallet_id are required for withdrawal inspection");
        }
        String partyKey = PayoutScope.key(model.getPartyId(), null);
        String walletKey = PayoutScope.key(model.getPartyId(), model.getWalletId());
        return cascade.apply(List.of(
                cascade.template(model, PayoutScope.GLOBAL),
                cascade.group(model, PayoutScope.GLOBAL),
                cascade.group(model, partyKey),
                cascade.group(model, walletKey),
                cascade.template(model, partyKey),
                cascade.template(model, walletKey)
        ), ResultStatus.HIGH_RISK);
    }
}
