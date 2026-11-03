package dev.vality.fraudbusters.management.utils;

import org.springframework.util.StringUtils;

/** Payout rules are scoped by party and wallet; an absent identifier is stored as an empty string. */
public final class PayoutScope {

    private PayoutScope() {
    }

    public static String identity(String value) {
        return value == null ? "" : value;
    }

    public static void validate(String shopId, String partyId, String walletId) {
        if (StringUtils.hasText(shopId) || StringUtils.hasText(walletId) && !StringUtils.hasText(partyId)) {
            throw new IllegalArgumentException("Invalid payout scope");
        }
    }
}
