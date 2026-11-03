package dev.vality.fraudbusters.fraud.resolver;

import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.model.OperationModel;
import dev.vality.fraudo.exception.UnresolvableFieldException;
import dev.vality.fraudo.resolver.FieldResolver;

/** Resolves rule fields that every operation has; subclasses add the fields of their own operation. */
public abstract class OperationModelFieldResolver<T extends OperationModel>
        implements FieldResolver<T, PaymentCheckedField> {

    @Override
    public String resolveValue(String fieldName, T model) {
        PaymentCheckedField field = resolveName(fieldName);
        return switch (field) {
            case BIN -> model.getBin();
            case IP -> model.getIp();
            case FINGERPRINT -> model.getFingerprint();
            case USER_ID -> model.getUserId();
            case EMAIL -> model.getEmail();
            case PHONE -> model.getPhone();
            case COUNTRY_BANK -> model.getBinCountryCode();
            case CARD_TOKEN -> model.getCardToken();
            case LAST_DIGITS -> model.getLastDigits();
            case PARTY_ID -> model.getPartyId();
            case CURRENCY -> model.getCurrency();
            case MASKED_PAN -> model.getBin() + model.getLastDigits();
            default -> resolveOperationValue(field, fieldName, model);
        };
    }

    @Override
    public PaymentCheckedField resolveName(String fieldName) {
        return PaymentCheckedField.getByValue(fieldName);
    }

    protected String resolveOperationValue(PaymentCheckedField field, String fieldName, T model) {
        throw new UnresolvableFieldException(fieldName);
    }

}
