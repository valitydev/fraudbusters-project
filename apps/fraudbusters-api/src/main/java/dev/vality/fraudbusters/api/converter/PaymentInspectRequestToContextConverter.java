package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.proxy_inspector.*;
import dev.vality.damsel.proxy_inspector.Invoice;
import dev.vality.damsel.proxy_inspector.InvoicePayment;
import dev.vality.damsel.proxy_inspector.Shop;
import dev.vality.swag.fraudbusters.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static dev.vality.fraudbusters.api.constants.PaymentResourceTypeConstants.BANK_CARD;

@Component
@RequiredArgsConstructor
public class PaymentInspectRequestToContextConverter implements Converter<PaymentInspectRequest, Context> {

    public static final String MOCK_UNUSED_DATA = "MOCK_UNUSED_DATA";
    private final CachToInternalDtoConverter cachToInternalDtoConverter;

    @Override
    public Context convert(PaymentInspectRequest request) {
        var payment = request.getPayment();
        Merchant merchant = payment.getMerchant();
        return new Context()
                .setPayment(new PaymentInfo()
                        .setShop(buildShop(merchant))
                        .setParty(buildParty(merchant))
                        .setInvoice(buildInvoice(payment))
                        .setPayment(buildInvoicePayment(payment)));
    }

    private Shop buildShop(Merchant merchant) {
        var shop = merchant.getShop();
        return new Shop()
                .setShopRef(new ShopConfigRef()
                        .setId(shop.getId()))
                .setName(shop.getName())
                .setCategory(new Category()
                        .setName(shop.getCategory())
                        .setDescription(MOCK_UNUSED_DATA))
                .setLocation(ShopLocation.url(shop.getLocation()));
    }

    private Party buildParty(Merchant merchant) {
        return new Party()
                .setPartyRef(new PartyConfigRef()
                        .setId(merchant.getId()));
    }

    private Invoice buildInvoice(Payment payment) {
        return new Invoice()
                .setId(payment.getId())
                .setDetails(new InvoiceDetails()
                        .setProduct(payment.getDescription()))
                .setCreatedAt(payment.getCreatedAt().toInstant().toString())
                .setDue(payment.getCreatedAt().toInstant().toString());
    }

    private InvoicePayment buildInvoicePayment(Payment payment) {
        InvoicePayment invoicePayment = new InvoicePayment()
                .setCost(cachToInternalDtoConverter.convert(payment.getCash()))
                .setId(payment.getId())
                .setCreatedAt(payment.getCreatedAt().toInstant().toString())
                .setPayer(Payer.payment_resource(new PaymentResourcePayer()
                        .setContactInfo(buildContactInfo(payment))
                        .setResource(buildResource(payment))));
        if (PayerType.RECURRENT.equals(payment.getPayerType())) {
            invoicePayment.setMakeRecurrent(true);
        }
        return invoicePayment;
    }

    private ContactInfo buildContactInfo(Payment payment) {
        Contact contact = Optional.ofNullable(payment.getCustomer())
                .map(Customer::getContact)
                .orElseGet(Contact::new);
        return new ContactInfo()
                .setEmail(contact.getEmail())
                .setPhoneNumber(contact.getPhone());
    }

    private DisposablePaymentResource buildResource(Payment payment) {
        return new DisposablePaymentResource()
                .setClientInfo(buildClientInfo(payment))
                .setPaymentTool(buildPaymentTool(payment.getPaymentResource()));
    }

    private ClientInfo buildClientInfo(Payment payment) {
        Device device = Optional.ofNullable(payment.getCustomer())
                .map(Customer::getDevice)
                .orElseGet(Device::new);
        return new ClientInfo()
                .setFingerprint(device.getFingerprint())
                .setIpAddress(device.getIp());
    }

    private PaymentTool buildPaymentTool(PaymentResource paymentResource) {
        return switch (paymentResource.getType()) {
            case BANK_CARD -> buildCard((dev.vality.swag.fraudbusters.model.BankCard) paymentResource);
            default -> null;
        };
    }

    private PaymentTool buildCard(dev.vality.swag.fraudbusters.model.BankCard bankCard) {
        return PaymentTool.bank_card(new BankCard()
                .setPaymentSystem(new PaymentSystemRef()
                        .setId(bankCard.getPaymentSystem()))
                .setIssuerCountry(CountryCode.valueOf(
                        bankCard.getCountryCode()))
                .setBankName(bankCard.getBankName())
                .setBin(bankCard.getBin())
                .setLastDigits(bankCard.getLastDigits())
                .setToken(bankCard.getCardToken()));
    }
}
