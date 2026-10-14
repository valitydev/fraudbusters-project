package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.FraudPayment;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import dev.vality.swag.fraudbusters.model.FraudPaymentsRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FraudPaymentsRequestToFraudPaymentsConverterTest {

    @Test
    void convert() {
        FraudPaymentsRequestToFraudPaymentsConverter fraudPaymentsRequestToFraudPaymentsConverter =
                new FraudPaymentsRequestToFraudPaymentsConverter();

        List<FraudPayment> fraudPayments =
                fraudPaymentsRequestToFraudPaymentsConverter.convert(new FraudPaymentsRequest()
                        .fraudPayments(
                                List.of(ApiBeanGenerator.initFraudPayment()))
                );

        assertNotNull(fraudPayments);
        assertFalse(fraudPayments.isEmpty());

        FraudPayment fraudPayment = fraudPayments.get(0);
        assertNotNull(fraudPayment);
        assertEquals(ApiBeanGenerator.COMMENT, fraudPayment.getComment());
        assertNotNull(fraudPayment.getEventTime());
        assertEquals(ApiBeanGenerator.PAYMENT_ID, fraudPayment.getId());
        assertEquals(ApiBeanGenerator.TYPE, fraudPayment.getType());
    }
}
