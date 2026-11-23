package dev.vality.fraudbusters.management.resource.payout;

import dev.vality.damsel.base.BoundType;
import dev.vality.damsel.base.TimestampInterval;
import dev.vality.damsel.base.TimestampIntervalBound;
import dev.vality.damsel.fraudbusters.Filter;
import dev.vality.damsel.fraudbusters.Sort;
import dev.vality.damsel.fraudbusters.SortOrder;

final class PayoutFilterFactory {

    static Filter forPayouts(String from, String to, String partyId, String walletId, String payoutId,
                                 String status, String email, String cardToken, String fingerprint, String maskedPan,
                                 String customerId, String userId, String providerCountry, String terminal) {
        return buildCommonFilter(from, to, partyId, walletId, payoutId, status, email,
                cardToken, fingerprint, maskedPan, customerId, userId)
                .setProviderCountry(providerCountry)
                .setTerminal(terminal);
    }

    static Filter forInspectResults(String from, String to, String partyId, String walletId, String payoutId,
                                    String status, String email, String cardToken, String fingerprint, String maskedPan,
                                    String customerId, String userId, String template, String rule) {
        return buildCommonFilter(from, to, partyId, walletId, payoutId, status, email,
                cardToken, fingerprint, maskedPan, customerId, userId)
                .setTemplate(template)
                .setRule(rule);
    }

    static Sort buildSort(String sortBy, String sortOrder) {
        return new Sort()
                .setField(sortBy)
                .setOrder(sortOrder != null ? SortOrder.valueOf(sortOrder) : SortOrder.DESC);
    }

    private static Filter buildCommonFilter(String from, String to, String partyId, String walletId,
                                            String payoutId, String status, String email, String cardToken,
                                            String fingerprint, String maskedPan, String customerId, String userId) {
        return new Filter()
                .setInterval(new TimestampInterval()
                        .setLowerBound(new TimestampIntervalBound()
                                .setBoundTime(from)
                                .setBoundType(BoundType.inclusive))
                        .setUpperBound(new TimestampIntervalBound()
                                .setBoundTime(to)
                                .setBoundType(BoundType.inclusive)))
                .setPartyId(partyId)
                .setWalletId(walletId)
                .setPaymentId(payoutId)
                .setStatus(status)
                .setEmail(email)
                .setCardToken(cardToken)
                .setFingerprint(fingerprint)
                .setMaskedPan(maskedPan)
                .setCustomerId(customerId)
                .setUserId(userId);
    }
}
