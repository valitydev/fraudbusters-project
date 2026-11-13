package dev.vality.fraudbusters.management.utils;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

/** Payout rules are scoped by party and wallet; an absent identifier is stored as an empty string. */
@UtilityClass
public class PayoutScope {

    public static final String GLOBAL = "GLOBAL";

    private static final int DEFAULT_PAGE_SIZE = 100;

    public static String replaceNullWithEmpty(String value) {
        return value == null ? "" : value;
    }

    public static void requirePartyForWallet(String partyId, String walletId) {
        if (StringUtils.hasText(walletId) && !StringUtils.hasText(partyId)) {
            throw new IllegalArgumentException("Invalid payout scope");
        }
    }

    /** The Kafka key of a payout reference; the same scope always gets the same key, as fraudbusters keys it. */
    public static String generateKey(String partyId, String walletId) {
        requirePartyForWallet(partyId, walletId);
        if (!StringUtils.hasText(partyId)) {
            return GLOBAL;
        }
        String party = "party:%d:%s".formatted(partyId.length(), partyId);
        return StringUtils.hasText(walletId)
                ? "%s:wallet:%d:%s".formatted(party, walletId.length(), walletId) : party;
    }

    public static int pageSize(Integer size) {
        return size == null || size <= 0 ? DEFAULT_PAGE_SIZE : size;
    }
}
