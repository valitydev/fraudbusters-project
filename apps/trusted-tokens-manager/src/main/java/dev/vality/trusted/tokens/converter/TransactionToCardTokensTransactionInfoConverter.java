package dev.vality.trusted.tokens.converter;

import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.damsel.fraudbusters.Withdrawal;
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

    public CardTokensTransactionInfo convertWithdrawal(Withdrawal withdrawal) {
        LocalDateTime localDateTime = LocalDateTime.parse(withdrawal.getEventTime(), DateTimeFormatter.ISO_DATE_TIME);
        return CardTokensTransactionInfo.builder()
                .lastWithdrawalId(withdrawal.getId())
                .token(withdrawal.getDestinationResource().getBankCard().getToken())
                .currency(withdrawal.getCost().getCurrency().getSymbolicCode())
                .year(localDateTime.getYear())
                .month(localDateTime.getMonth().getValue())
                .build();
    }

}
