package dev.vality.fraudbusters.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Columns of fraud.withdrawal that rules may filter by, keyed by the rule field name; SQL column names are taken
 * only from here, never from rule text. Only data that fraudbusters-mg-connector writes from fistful withdrawal
 * events is listed.
 */
public enum PayoutHistoryColumn {

    PARTY_ID("party_id", "partyId"),
    WALLET_ID("wallet_id", "walletId"),
    CURRENCY("currency", "currency"),
    CARD_TOKEN("card_token", "cardToken"),
    BIN("bin", "bin"),
    LAST_DIGITS("last_digits", "lastDigits"),
    ISSUER_COUNTRY("country_bank", "issuerCountry"),
    USER_ID("user_id", "userId"),
    IP("ip", "ip"),
    FINGERPRINT("fingerprint", "fingerprint"),
    EMAIL("email", "email"),
    PHONE("phone", "phone");

    private static final Map<String, PayoutHistoryColumn> BY_FIELD = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(column -> column.field, Function.identity()));

    private final String field;
    private final String column;

    PayoutHistoryColumn(String field, String column) {
        this.field = field;
        this.column = column;
    }

    public static Optional<String> findColumn(String fieldName) {
        return Optional.ofNullable(BY_FIELD.get(fieldName))
                .map(PayoutHistoryColumn::getColumn);
    }

    public String getColumn() {
        return column;
    }

}
