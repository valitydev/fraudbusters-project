package dev.vality.fraudbusters.converter;

import dev.vality.damsel.fraudbusters.FraudPayment;
import dev.vality.fraudbusters.domain.CheckedPayment;
import dev.vality.fraudbusters.domain.FraudPaymentRow;
import dev.vality.fraudbusters.service.PaymentInfoService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FraudPaymentToRowConverterTest {

    private final PaymentInfoService paymentInfoService = mock(PaymentInfoService.class);
    private final FraudPaymentToRowConverter converter = new FraudPaymentToRowConverter(paymentInfoService);

    @Test
    void convertsFraudPaymentSentByApiWithIsoTimeAndKeepsTypeAndComment() {
        CheckedPayment checkedPayment = new CheckedPayment();
        checkedPayment.setId("payment-1");
        when(paymentInfoService.findPaymentByIdAndTimestamp(LocalDate.parse("2026-11-16"), "payment-1"))
                .thenReturn(checkedPayment);

        FraudPaymentRow row = converter.convert(new FraudPayment()
                .setId("payment-1")
                .setEventTime("2026-11-16T10:15:30Z")
                .setType("card_testing")
                .setComment("confirmed by the bank"));

        assertEquals("payment-1", row.getId());
        assertEquals("card_testing", row.getType());
        assertEquals("confirmed by the bank", row.getComment());
    }
}
