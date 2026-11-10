package dev.vality.fraudbusters.util;

import org.springframework.util.StringUtils;

public final class PayoutScope {

    public static final String GLOBAL = "GLOBAL";

    private PayoutScope() {
    }

    public static String generateKey(String partyId, String walletId) {
        if (!StringUtils.hasText(partyId)) {
            if (StringUtils.hasText(walletId)) {
                throw new IllegalArgumentException("wallet_id requires party_id");
            }
            return GLOBAL;
        }
        String party = "party:" + partyId.length() + ":" + partyId;
        return StringUtils.hasText(walletId) ? party + ":wallet:" + walletId.length() + ":" + walletId : party;
    }
}
