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
        WithdrawalsRequestToWithdrawalsConverter converter = new WithdrawalsRequestToWithdrawalsConverter(
                cachToInternalDtoConverter,
                errorToInternalDtoConverter,
                providerToInternalDtoConverter,
                new PaymentResourceToResourceConverter(new BankCardToInternalDtoConverter())
        );

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
}
