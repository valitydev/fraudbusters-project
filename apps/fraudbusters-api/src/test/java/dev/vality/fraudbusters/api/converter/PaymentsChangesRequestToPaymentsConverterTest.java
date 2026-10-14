package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.damsel.fraudbusters.PaymentStatus;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import dev.vality.swag.fraudbusters.model.PaymentsChangesRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentsChangesRequestToPaymentsConverterTest {

    @Mock
    CachToInternalDtoConverter cachToInternalDtoConverter;
    @Mock
    CustomerToInternalDtoConverter customerToInternalDtoConverter;
    @Mock
    PaymentResourceToPaymentToolConverter paymentResourceToPaymentToolConverter;
    @Mock
    ProviderToInternalDtoConverter providerToInternalDtoConverter;
    @Mock
    MerchantToInternalDtoConverter merchantToInternalDtoConverter;
    @Mock
    ErrorToInternalDtoConverter errorToInternalDtoConverter;

    @Test
    void convert() {
        PaymentsChangesRequestToPaymentsConverter converter = new PaymentsChangesRequestToPaymentsConverter(
                cachToInternalDtoConverter,
                customerToInternalDtoConverter,
                paymentResourceToPaymentToolConverter,
                providerToInternalDtoConverter,
                merchantToInternalDtoConverter,
                errorToInternalDtoConverter
        );

        List<Payment> payments =
                converter.convert(new PaymentsChangesRequest()
                        .paymentsChanges(
                                List.of(ApiBeanGenerator.initPaymentChange()))
                );

        assertNotNull(payments);
        assertFalse(payments.isEmpty());

        Payment payment = payments.get(0);
        assertNotNull(payment);
        assertEquals(ApiBeanGenerator.ID, payment.getId());
        assertNotNull(payment.getEventTime());
        assertEquals(PaymentStatus.captured, payment.getStatus());
        assertFalse(payment.mobile);
        assertTrue(payment.recurrent);
    }
}
