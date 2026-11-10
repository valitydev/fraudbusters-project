package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.damsel.fraudbusters.WithdrawalStatus;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import dev.vality.swag.fraudbusters.model.WithdrawalsRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WithdrawalsRequestToWithdrawalsConverterTest {

    @Mock
    CachToInternalDtoConverter cachToInternalDtoConverter;
    @Mock
    ProviderToInternalDtoConverter providerToInternalDtoConverter;
    @Mock
    ErrorToInternalDtoConverter errorToInternalDtoConverter;

    @Test
    void convert() {
        var converter = converter();

        List<Withdrawal> withdrawals = converter.convert(new WithdrawalsRequest().withdrawals(
                List.of(ApiBeanGenerator.initWithdrawal())));

        assertNotNull(withdrawals);
        assertFalse(withdrawals.isEmpty());

        Withdrawal withdrawal = withdrawals.get(0);
        assertNotNull(withdrawal);
        assertEquals(ApiBeanGenerator.ID, withdrawal.getId());
        assertNotNull(withdrawal.getEventTime());
        assertEquals(WithdrawalStatus.succeeded, withdrawal.getStatus());
        assertEquals(ApiBeanGenerator.ACCOUNT_ID, withdrawal.getAccount().getId());
    }

    @Test
    void forwardsPartyWalletAndCustomerIdentity() {
        var item = ApiBeanGenerator.initWithdrawal()
                .partyId("synthetic-party")
                .walletId("synthetic-wallet")
                .customer(new dev.vality.swag.fraudbusters.model.Customer()
                        .customerId("synthetic-customer"));
        var converter = converter();
        var request = new WithdrawalsRequest().withdrawals(List.of(item));
        var withdrawal = converter.convert(request).get(0);
        assertEquals("synthetic-party", withdrawal.getPartyId());
        assertEquals("synthetic-wallet", withdrawal.getWalletId());
        assertEquals("synthetic-customer", withdrawal.getClientInfo().getCustomerId());
        assertFalse(withdrawal.getClientInfo().isSetUserId());
        item.getCustomer().setUserId("synthetic-user");
        assertEquals("synthetic-user", converter.convert(request).get(0).getClientInfo().getUserId());
    }

    private WithdrawalsRequestToWithdrawalsConverter converter() {
        return new WithdrawalsRequestToWithdrawalsConverter(
                cachToInternalDtoConverter,
                errorToInternalDtoConverter,
                providerToInternalDtoConverter,
                new PaymentResourceToResourceConverter(new BankCardToInternalDtoConverter()),
                new CustomerToInternalDtoConverter()
        );
    }

}
