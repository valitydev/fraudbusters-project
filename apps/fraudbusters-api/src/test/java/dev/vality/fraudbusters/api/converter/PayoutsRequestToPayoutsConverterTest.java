package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.damsel.fraudbusters.PayoutStatus;
import dev.vality.fraudbusters.api.exceptions.InvalidRequestException;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import dev.vality.swag.fraudbusters.model.PayoutsRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PayoutsRequestToPayoutsConverterTest {

    @Mock
    CachToInternalDtoConverter cachToInternalDtoConverter;
    @Mock
    ProviderToInternalDtoConverter providerToInternalDtoConverter;
    @Mock
    ErrorToInternalDtoConverter errorToInternalDtoConverter;

    @Test
    void convert() {
        var converter = createConverter();

        List<Payout> payouts = converter.convert(new PayoutsRequest().payouts(
                List.of(ApiBeanGenerator.initPayout())));

        assertNotNull(payouts);
        assertFalse(payouts.isEmpty());

        Payout payout = payouts.get(0);
        assertNotNull(payout);
        assertEquals(ApiBeanGenerator.ID, payout.getId());
        assertNotNull(payout.getEventTime());
        assertEquals(PayoutStatus.succeeded, payout.getStatus());
        assertEquals(ApiBeanGenerator.ACCOUNT_ID, payout.getAccount().getId());
    }

    @Test
    void walletWithoutPartyIsAnInvalidRequest() {
        var request = new PayoutsRequest().payouts(List.of(ApiBeanGenerator.initPayout()
                .walletId("synthetic-wallet")));

        assertThrows(InvalidRequestException.class, () -> createConverter().convert(request));
    }

    @Test
    void forwardsPartyWalletAndCustomerIdentity() {
        var item = ApiBeanGenerator.initPayout()
                .partyId("synthetic-party")
                .walletId("synthetic-wallet")
                .customer(new dev.vality.swag.fraudbusters.model.Customer()
                        .customerId("synthetic-customer"));
        var converter = createConverter();
        var request = new PayoutsRequest().payouts(List.of(item));
        var payout = converter.convert(request).get(0);
        assertEquals("synthetic-party", payout.getPartyId());
        assertEquals("synthetic-wallet", payout.getWalletId());
        assertEquals("synthetic-customer", payout.getClientInfo().getCustomerId());
        assertFalse(payout.getClientInfo().isSetUserId());
        item.getCustomer().setUserId("synthetic-user");
        assertEquals("synthetic-user", converter.convert(request).get(0).getClientInfo().getUserId());
    }

    private PayoutsRequestToPayoutsConverter createConverter() {
        return new PayoutsRequestToPayoutsConverter(
                cachToInternalDtoConverter,
                errorToInternalDtoConverter,
                providerToInternalDtoConverter,
                new PaymentResourceToResourceConverter(new BankCardToInternalDtoConverter()),
                new CustomerToInternalDtoConverter()
        );
    }

}
