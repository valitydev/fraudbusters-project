package dev.vality.fraudbusters.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Payout history columns that payments do not have; the shared ones stay in {@link PaymentField}.
 */
@AllArgsConstructor
public enum WithdrawalField implements FilterField {

    WALLET_ID("walletId");

    @Getter
    private final String value;

}
