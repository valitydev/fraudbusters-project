package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.domain.BankCard;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BankCardToInternalDtoConverterTest {

    @Test
    void convert() throws TException {
        BankCardToInternalDtoConverter bankCardToInternalDtoConverter = new BankCardToInternalDtoConverter();

        BankCard bankCard = bankCardToInternalDtoConverter.convert(ApiBeanGenerator.initBankCard());

        assertNotNull(bankCard);
        bankCard.validate();

        assertEquals(ApiBeanGenerator.BANK_NAME, bankCard.bank_name);
        assertEquals(ApiBeanGenerator.BIN, bankCard.bin);
        assertEquals(ApiBeanGenerator.CARD_TOKEN, bankCard.token);
        assertEquals(ApiBeanGenerator.LAST_DIGITS, bankCard.last_digits);
    }
}
