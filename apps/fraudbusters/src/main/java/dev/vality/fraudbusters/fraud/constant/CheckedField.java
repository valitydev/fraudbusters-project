package dev.vality.fraudbusters.fraud.constant;

import java.util.HashMap;
import java.util.Map;

public enum CheckedField {

    EMAIL("email"),
    IP("ip"),
    FINGERPRINT("fingerprint"),
    USER_ID("user_id"),
    COUNTRY_BANK("country_bank"),
    COUNTRY_IP("country_ip"),
    BIN("bin"),
    LAST_DIGITS("last_digits"),
    CURRENCY("currency"),
    SHOP_ID("shop_id"),
    PARTY_ID("party_id"),
    WALLET_ID("wallet_id"),
    MOBILE("mobile"),
    RECURRENT("recurrent"),
    CARD_TOKEN("card_token"),
    PHONE("phone"),
    MASKED_PAN("masked_pan");

    private static final Map<String, CheckedField> VALUE_MAP = new HashMap<>();

    static {
        for (CheckedField value : CheckedField.values()) {
            VALUE_MAP.put(value.value, value);
        }
    }

    private final String value;

    CheckedField(String value) {
        this.value = value;
    }

    public static CheckedField getByValue(String value) {
        return VALUE_MAP.get(value);
    }

}
