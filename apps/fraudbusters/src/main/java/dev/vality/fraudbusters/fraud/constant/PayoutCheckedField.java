package dev.vality.fraudbusters.fraud.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/** Rule fields that a withdrawal inspection request (proxy_inspector.WithdrawalContext) carries. */
@AllArgsConstructor
public enum PayoutCheckedField {

    EMAIL("email"),
    IP("ip"),
    FINGERPRINT("fingerprint"),
    USER_ID("user_id"),
    COUNTRY_BANK("country_bank"),
    BIN("bin"),
    LAST_DIGITS("last_digits"),
    CURRENCY("currency"),
    PARTY_ID("party_id"),
    WALLET_ID("wallet_id"),
    CARD_TOKEN("card_token"),
    PHONE("phone"),
    MASKED_PAN("masked_pan");

    private static final Map<String, PayoutCheckedField> VALUE_MAP = new HashMap<>();

    static {
        for (PayoutCheckedField value : PayoutCheckedField.values()) {
            VALUE_MAP.put(value.value, value);
        }
    }

    @Getter
    private final String value;

    public static PayoutCheckedField getByValue(String value) {
        return VALUE_MAP.get(value);
    }

}
