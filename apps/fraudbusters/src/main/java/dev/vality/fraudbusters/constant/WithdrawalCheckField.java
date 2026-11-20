package dev.vality.fraudbusters.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum WithdrawalCheckField implements FilterField {

    EVENT_TIME("eventTime"),
    ID("id"),
    PARTY_ID("partyId"),
    WALLET_ID("walletId"),
    AMOUNT("amount"),
    CURRENCY("currency"),
    CARD_TOKEN("cardToken"),
    BIN("bin"),
    LAST_DIGITS("lastDigits"),
    PAYMENT_SYSTEM("paymentSystem"),
    ISSUER_COUNTRY("issuerCountry"),
    CUSTOMER_ID("customerId"),
    USER_ID("userId"),
    IP("ip"),
    FINGERPRINT("fingerprint"),
    EMAIL("email"),
    PHONE("phone"),
    RESULT_STATUS("resultStatus"),
    CHECKED_RULE("checkedRule"),
    CHECKED_TEMPLATE("checkedTemplate");

    @Getter
    private final String value;

}
