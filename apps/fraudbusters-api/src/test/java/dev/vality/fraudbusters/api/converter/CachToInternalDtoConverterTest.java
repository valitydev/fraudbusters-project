package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.domain.Cash;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CachToInternalDtoConverterTest {

    @Test
    void convert() throws TException {
        CachToInternalDtoConverter cachToInternalDtoConverter = new CachToInternalDtoConverter();

        Cash cash = cachToInternalDtoConverter.convert(ApiBeanGenerator.initCash());

        assertNotNull(cash);
        cash.validate();

        assertEquals(ApiBeanGenerator.AMOUNT, cash.getAmount());
        assertEquals(ApiBeanGenerator.CURRENCY, cash.getCurrency().getSymbolicCode());
    }
}
