package dev.vality.fraudbusters.converter;

import dev.vality.damsel.proxy_inspector.WithdrawalContext;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.util.TimestampUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PayoutContextToModelConverter implements Converter<WithdrawalContext, PayoutModel> {

    @Override
    public PayoutModel convert(WithdrawalContext context) {
        PayoutModel model = new PayoutModel();
        model.setPayoutId(context.getWithdrawalId());
        model.setPartyId(context.getParty().getPartyRef().getId());
        model.setWalletId(context.getWalletRef().getId());
        var payout = context.getWithdrawal();
        model.setAmount(payout.getBody().getAmount());
        model.setCurrency(payout.getBody().getCurrency().getSymbolicCode());
        model.setTimestamp(payout.isSetCreatedAt()
                ? TimestampUtil.parseInstantFromString(payout.getCreatedAt()).toEpochMilli()
                : System.currentTimeMillis());
        if (payout.isSetClientIdentity()) {
            model.setCustomerId(payout.getClientIdentity().getCustomerId());
            model.setUserId(payout.getClientIdentity().getUserId());
        }
        if (payout.isSetClientInfo()) {
            model.setIp(payout.getClientInfo().getIpAddress());
            model.setFingerprint(payout.getClientInfo().getFingerprint());
        }
        if (payout.isSetContactInfo()) {
            model.setEmail(payout.getContactInfo().getEmail());
            model.setPhone(payout.getContactInfo().getPhoneNumber());
        }
        if (payout.getDestination().isSetBankCard()) {
            var card = payout.getDestination().getBankCard();
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
