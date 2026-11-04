package dev.vality.fraudbusters.fraud.payout;

import dev.vality.fraudbusters.fraud.constant.CheckedField;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.OperationModel;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudo.exception.UnresolvableFieldException;

/** Column names are selected exclusively from this whitelist, never from rule text. */
public final class PayoutFields {

    private PayoutFields() {
    }

    public static String column(CheckedField field) {
        if (field == null) {
            throw new UnresolvableFieldException("null");
        }
        return switch (field) {
            case USER_ID -> "userId";
            case PARTY_ID -> "partyId";
            case WALLET_ID -> "walletId";
            case CURRENCY -> "currency";
            case CARD_TOKEN -> "cardToken";
            case BIN -> "bin";
            case LAST_DIGITS -> "lastDigits";
            case COUNTRY_BANK -> "issuerCountry";
            case IP, COUNTRY_IP -> "ip";
            case EMAIL -> "email";
            case PHONE -> "phone";
            case FINGERPRINT -> "fingerprint";
            default -> throw new UnresolvableFieldException(field.name());
        };
    }

    public static FieldModel resolve(CheckedField field, OperationModel model) {
        Object value = switch (field) {
            case USER_ID -> model.getUserId();
            case PARTY_ID -> model.getPartyId();
            case WALLET_ID -> model instanceof PayoutModel payout ? payout.getWalletId() : null;
            case CURRENCY -> model.getCurrency();
            case CARD_TOKEN -> model.getCardToken();
            case BIN -> model.getBin();
            case LAST_DIGITS -> model.getLastDigits();
            case COUNTRY_BANK -> model.getBinCountryCode();
            case IP, COUNTRY_IP -> model.getIp();
            case EMAIL -> model.getEmail();
            case PHONE -> model.getPhone();
            case FINGERPRINT -> model.getFingerprint();
            default -> throw new UnresolvableFieldException(field.name());
        };
        return new FieldModel(column(field), value);
    }
}
