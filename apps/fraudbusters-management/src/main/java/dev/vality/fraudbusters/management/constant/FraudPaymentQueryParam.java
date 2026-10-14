package dev.vality.fraudbusters.management.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FraudPaymentQueryParam {

    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String CURRENCY = "currency";
    public static final String SHOP_ID = "shopId";
    public static final String PARTY_ID = "partyId";
}
