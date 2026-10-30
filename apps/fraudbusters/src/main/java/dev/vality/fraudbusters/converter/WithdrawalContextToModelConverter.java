package dev.vality.fraudbusters.converter;

import dev.vality.damsel.proxy_inspector.WithdrawalContext;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.util.TimestampUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalContextToModelConverter implements Converter<WithdrawalContext, PaymentModel> {

    @Override
    public PaymentModel convert(WithdrawalContext context) {
        PaymentModel model = new PaymentModel();
        model.setOperationType(dev.vality.damsel.fraudbusters.OperationType.payout);
        model.setPartyId(context.getParty().getPartyRef().getId());
        model.setWalletId(context.getWalletRef().getId());
        var withdrawal = context.getWithdrawal();
        model.setAmount(withdrawal.getBody().getAmount());
        model.setCurrency(withdrawal.getBody().getCurrency().getSymbolicCode());
        model.setTimestamp(withdrawal.isSetCreatedAt()
                ? TimestampUtil.parseInstantFromString(withdrawal.getCreatedAt()).toEpochMilli()
                : System.currentTimeMillis());
        if (withdrawal.isSetClientIdentity()) {
            model.setCustomerId(withdrawal.getClientIdentity().getCustomerId());
            model.setUserId(withdrawal.getClientIdentity().getUserId());
        }
        if (withdrawal.isSetClientInfo()) {
            model.setIp(withdrawal.getClientInfo().getIpAddress());
            model.setFingerprint(withdrawal.getClientInfo().getFingerprint());
        }
        if (withdrawal.isSetContactInfo()) {
            model.setEmail(withdrawal.getContactInfo().getEmail());
            model.setPhone(withdrawal.getContactInfo().getPhoneNumber());
        }
        if (withdrawal.getDestination().isSetBankCard()) {
            var card = withdrawal.getDestination().getBankCard();
            model.setBin(card.getBin());
            model.setLastDigits(card.getLastDigits());
            model.setCardToken(card.getToken());
            model.setBinCountryCode(card.isSetIssuerCountry() ? card.getIssuerCountry().name() : null);
            model.setPaymentSystem(card.isSetPaymentSystem() ? card.getPaymentSystem().getId() : null);
            model.setCardCategory(card.getCategory());
        }
        return model;
    }
}
