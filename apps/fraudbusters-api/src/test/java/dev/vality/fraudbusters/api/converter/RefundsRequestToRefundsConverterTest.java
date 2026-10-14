package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.Refund;
import dev.vality.damsel.fraudbusters.RefundStatus;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import dev.vality.swag.fraudbusters.model.RefundsRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RefundsRequestToRefundsConverterTest {

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
        RefundsRequestToRefundsConverter converter = new RefundsRequestToRefundsConverter(
                cachToInternalDtoConverter,
                customerToInternalDtoConverter,
                paymentResourceToPaymentToolConverter,
                providerToInternalDtoConverter,
                merchantToInternalDtoConverter,
                errorToInternalDtoConverter
        );

        List<Refund> refunds = converter.convert(new RefundsRequest().refunds(
                List.of(ApiBeanGenerator.initRefund())));

        assertNotNull(refunds);
        assertFalse(refunds.isEmpty());

        Refund refund = refunds.get(0);
        assertNotNull(refund);
        assertEquals(ApiBeanGenerator.ID, refund.getId());
        assertNotNull(refund.getEventTime());
        assertEquals(RefundStatus.succeeded, refund.getStatus());
        assertEquals(ApiBeanGenerator.PAYMENT_ID, refund.getPaymentId());
    }
}
