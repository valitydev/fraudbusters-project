package dev.vality.fraudbusters.fraud.payout;

import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.damsel.proxy_inspector.WithdrawalContext;
import dev.vality.fraudbusters.converter.WithdrawalContextToModelConverter;
import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.stream.impl.RuleApplierImpl;
import dev.vality.fraudbusters.stream.impl.RuleCascade;
import dev.vality.fraudbusters.util.CheckedResultFactory;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.payment.visitor.impl.FirstFindVisitorImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PayoutInspectionService {

    private final RuleCascade cascade;
    private final WithdrawalContextToModelConverter modelConverter;

    public PayoutInspectionService(PayoutRulePools pools,
                                   @Qualifier("payoutRuleVisitor")
                                   FirstFindVisitorImpl<PaymentModel, PaymentCheckedField> visitor,
                                   CheckedResultFactory checkedResultFactory,
                                   WithdrawalContextToModelConverter modelConverter) {
        this.modelConverter = modelConverter;
        cascade = new RuleCascade(new RuleApplierImpl<>(visitor, pools.getTemplates(), checkedResultFactory, true),
                pools.getGroups(), pools.getReferences(), pools.getGroupReferences());
    }

    public CheckedResultModel inspect(WithdrawalContext context) {
        PaymentModel model = modelConverter.convert(context);
        if (!StringUtils.hasText(model.getPartyId()) || !StringUtils.hasText(model.getWalletId())) {
            throw new IllegalArgumentException("party_id and wallet_id are required for withdrawal inspection");
        }
        return cascade.applyWithNotifications(model, PayoutScope.GLOBAL,
                PayoutScope.key(model.getPartyId(), null), PayoutScope.key(model.getPartyId(), model.getWalletId()),
                ResultStatus.HIGH_RISK);
    }
}
