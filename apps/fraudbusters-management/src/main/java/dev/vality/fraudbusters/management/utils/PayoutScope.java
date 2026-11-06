package dev.vality.fraudbusters.management.utils;

import org.springframework.util.StringUtils;

/** Payout rules are scoped by party and wallet; an absent identifier is stored as an empty string. */
public final class PayoutScope {

    public static final String GLOBAL = "GLOBAL";

    private static final int DEFAULT_PAGE_SIZE = 100;

    private PayoutScope() {
    }

    public static String normalizeId(String value) {
        return value == null ? "" : value;
    }

    public static void validate(String partyId, String walletId) {
        if (StringUtils.hasText(walletId) && !StringUtils.hasText(partyId)) {
            throw new IllegalArgumentException("Invalid payout scope");
        }
    }

    /** The Kafka key of a payout reference; the same scope always gets the same key, as fraudbusters keys it. */
    public static String generateKey(String partyId, String walletId) {
        validate(partyId, walletId);
        if (!StringUtils.hasText(partyId)) {
            return GLOBAL;
        }
        String party = "party:" + partyId.length() + ":" + partyId;
        return StringUtils.hasText(walletId) ? party + ":wallet:" + walletId.length() + ":" + walletId : party;
    }

    public static int pageSize(Integer size) {
        return size == null || size <= 0 ? DEFAULT_PAGE_SIZE : size;
    }
}
