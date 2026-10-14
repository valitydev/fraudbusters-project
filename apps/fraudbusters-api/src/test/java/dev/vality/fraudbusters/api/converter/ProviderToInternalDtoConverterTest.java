package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.ProviderInfo;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProviderToInternalDtoConverterTest {

    @Test
    void convert() throws TException {
        ProviderToInternalDtoConverter converter = new ProviderToInternalDtoConverter();

        ProviderInfo providerInfo = converter.convert(ApiBeanGenerator.initProvider());

        assertNotNull(providerInfo);
        providerInfo.validate();

        assertEquals(ApiBeanGenerator.PROVIDER_ID, providerInfo.getProviderId());
        assertEquals(ApiBeanGenerator.RUS, providerInfo.getCountry());
        assertEquals(ApiBeanGenerator.TERMINAL_ID, providerInfo.getTerminalId());
    }
}
