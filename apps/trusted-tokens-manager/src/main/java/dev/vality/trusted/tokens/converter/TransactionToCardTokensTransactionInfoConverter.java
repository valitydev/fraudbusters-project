package dev.vality.trusted.tokens.converter;

import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.trusted.tokens.model.CardTokensTransactionInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TransactionToCardTokensTransactionInfoConverter {

    public CardTokensTransactionInfo convertPayment(Payment payment) {
        LocalDateTime localDateTime = LocalDateTime.parse(payment.getEventTime(), DateTimeFormatter.ISO_DATE_TIME);
        return CardTokensTransactionInfo.builder()
                .lastPaymentId(payment.getId())
                .token(payment.getPaymentTool().getBankCard().getToken())
                .currency(payment.getCost().getCurrency().getSymbolicCode())
                .amount(payment.getCost().getAmount())
                .year(localDateTime.getYear())
                .month(localDateTime.getMonth().getValue())
                .build();
    }

    public CardTokensTransactionInfo convertPayout(Payout payout) {
        LocalDateTime localDateTime = LocalDateTime.parse(payout.getEventTime(), DateTimeFormatter.ISO_DATE_TIME);
        return CardTokensTransactionInfo.builder()
                .lastPayoutId(payout.getId())
                .token(payout.getDestinationResource().getBankCard().getToken())
                .currency(payout.getCost().getCurrency().getSymbolicCode())
                .year(localDateTime.getYear())
                .month(localDateTime.getMonth().getValue())
                .build();
    }

}
