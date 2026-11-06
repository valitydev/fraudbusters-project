package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.ClientInfo;
import dev.vality.swag.fraudbusters.model.Contact;
import dev.vality.swag.fraudbusters.model.Customer;
import dev.vality.swag.fraudbusters.model.Device;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CustomerToInternalDtoConverter
        implements Converter<Customer, ClientInfo> {

    @Override
    public ClientInfo convert(Customer customer) {
        ClientInfo clientInfo = new ClientInfo();
        Device device = customer.getDevice();
        if (Objects.nonNull(device)) {
            clientInfo.setFingerprint(device.getFingerprint());
            clientInfo.setIp(device.getIp());
        }
        Contact contact = customer.getContact();
        if (Objects.nonNull(contact)) {
            clientInfo.setEmail(contact.getEmail());
            clientInfo.setPhone(contact.getPhone());
        }
        return clientInfo;
    }

}
