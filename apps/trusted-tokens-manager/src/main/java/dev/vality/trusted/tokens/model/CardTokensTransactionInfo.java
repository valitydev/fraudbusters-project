package dev.vality.trusted.tokens.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardTokensTransactionInfo {

    public String lastPaymentId;
    public String lastWithdrawalId;
    public String token;
    public String currency;
    public long amount;
    public int year;
    public int month;
}
