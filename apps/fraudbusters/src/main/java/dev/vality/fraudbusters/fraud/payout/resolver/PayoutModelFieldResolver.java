package dev.vality.fraudbusters.fraud.payout.resolver;

import dev.vality.fraudbusters.fraud.constant.PayoutCheckedField;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudo.exception.UnresolvableFieldException;
import dev.vality.fraudo.resolver.FieldResolver;

public class PayoutModelFieldResolver implements FieldResolver<PayoutModel, PayoutCheckedField> {

    @Override
    public String resolveValue(String fieldName, PayoutModel payoutModel) {
        PayoutCheckedField field = resolveName(fieldName);
        if (field == null) {
            throw new UnresolvableFieldException(fieldName);
        }
        return switch (field) {
            case EMAIL -> payoutModel.getEmail();
            case IP -> payoutModel.getIp();
            case FINGERPRINT -> payoutModel.getFingerprint();
            case USER_ID -> payoutModel.getUserId();
            case COUNTRY_BANK -> payoutModel.getBinCountryCode();
            case BIN -> payoutModel.getBin();
            case LAST_DIGITS -> payoutModel.getLastDigits();
            case CURRENCY -> payoutModel.getCurrency();
            case PARTY_ID -> payoutModel.getPartyId();
            case WALLET_ID -> payoutModel.getWalletId();
            case CARD_TOKEN -> payoutModel.getCardToken();
            case PHONE -> payoutModel.getPhone();
            case MASKED_PAN -> payoutModel.getBin() + payoutModel.getLastDigits();
        };
    }

    @Override
    public PayoutCheckedField resolveName(String fieldName) {
        return PayoutCheckedField.getByValue(fieldName);
    }

}
