package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.ClientInfo;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomerToInternalDtoConverterTest {

    @Test
    void convert() throws TException {
        CustomerToInternalDtoConverter customerToInternalDtoConverter = new CustomerToInternalDtoConverter();

        ClientInfo clientInfo = customerToInternalDtoConverter.convert(ApiBeanGenerator.initCustomer());

        assertNotNull(clientInfo);
        clientInfo.validate();

        assertEquals(ApiBeanGenerator.EMAIL, clientInfo.getEmail());
        assertEquals(ApiBeanGenerator.FINGERPRINT, clientInfo.getFingerprint());
        assertEquals(ApiBeanGenerator.IP, clientInfo.getIp());
    }
}
