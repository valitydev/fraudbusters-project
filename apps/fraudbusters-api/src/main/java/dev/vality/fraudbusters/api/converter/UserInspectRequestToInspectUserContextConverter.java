package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.ClientInfo;
import dev.vality.damsel.fraudbusters.InspectUserContext;
import dev.vality.damsel.fraudbusters.ShopContext;
import dev.vality.swag.fraudbusters.model.Contact;
import dev.vality.swag.fraudbusters.model.Customer;
import dev.vality.swag.fraudbusters.model.Merchant;
import dev.vality.swag.fraudbusters.model.UserInspectRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserInspectRequestToInspectUserContextConverter
        implements Converter<UserInspectRequest, InspectUserContext> {

    @Override
    public InspectUserContext convert(UserInspectRequest request) {
        ClientInfo userInfo = buildContactInfo(request.getCustomer());
        List<ShopContext> shopContexts = Optional.ofNullable(request.getMerchants())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::buildShopContext)
                .collect(Collectors.toList());
        return new InspectUserContext()
                .setUserInfo(userInfo)
                .setShopList(shopContexts);
    }

    private ClientInfo buildContactInfo(Customer customer) {
        Contact contact = Optional.ofNullable(customer)
                .map(Customer::getContact)
                .orElseGet(Contact::new);
        ClientInfo clientInfo = new ClientInfo();
        if (customer != null && customer.getDevice() != null) {
            clientInfo.setFingerprint(customer.getDevice().getFingerprint())
                    .setIp(customer.getDevice().getIp());
        }
        return clientInfo
                .setEmail(contact.getEmail())
                .setPhone(contact.getPhone());
    }

    private ShopContext buildShopContext(Merchant merchant) {
        return new ShopContext()
                .setPartyId(merchant.getId())
                .setShopId(merchant.getShop().getId());
    }

}
