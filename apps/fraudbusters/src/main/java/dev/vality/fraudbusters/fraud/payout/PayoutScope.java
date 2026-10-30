package dev.vality.fraudbusters.fraud.payout;

import org.springframework.util.StringUtils;

public final class PayoutScope {

    public static final String GLOBAL = "GLOBAL";

    private PayoutScope() {
    }

    public static String key(String partyId, String walletId) {
        if (!StringUtils.hasText(partyId)) {
            if (StringUtils.hasText(walletId)) {
                throw new IllegalArgumentException("wallet_id requires party_id");
            }
            return GLOBAL;
        }
        String party = "party:" + partyId.length() + ":" + partyId;
        return StringUtils.hasText(walletId) ? party + ":wallet:" + walletId.length() + ":" + walletId : party;
    }

    public static void validate(String shopId, String partyId, String walletId) {
        if (StringUtils.hasText(shopId)) {
            throw new IllegalArgumentException("shop_id is not supported for payouts");
        }
        key(partyId, walletId);
    }
}
