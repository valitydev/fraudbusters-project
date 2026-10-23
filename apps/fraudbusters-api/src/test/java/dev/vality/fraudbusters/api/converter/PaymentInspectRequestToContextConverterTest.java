package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.proxy_inspector.Context;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import dev.vality.swag.fraudbusters.model.PaymentInspectRequest;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TProtocolException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentInspectRequestToContextConverterTest {

    @ParameterizedTest
    @CsvSource({"synthetic-customer,Synthetic-User", "synthetic-customer,", ",Synthetic-User"})
    void invoiceIdentityIsPreserved(String customerId, String userId) {
        var converter = new PaymentInspectRequestToContextConverter(new CachToInternalDtoConverter());
        var payment = ApiBeanGenerator.initPayment();
        payment.setCustomer(new dev.vality.swag.fraudbusters.model.Customer()
                .customerId(customerId).userId(userId));
        var context = converter.convert(new PaymentInspectRequest().payment(payment));
        var identity = context.getPayment().getInvoice().getClientIdentity();
        assertNotNull(identity);
        assertEquals(customerId, identity.getCustomerId());
        assertEquals(userId, identity.getUserId());
    }

    @Test
    void invoiceIdentityIsAbsentWithoutIds() {
        var converter = new PaymentInspectRequestToContextConverter(new CachToInternalDtoConverter());
        var payment = ApiBeanGenerator.initPayment();
        payment.setCustomer(new dev.vality.swag.fraudbusters.model.Customer());
        assertFalse(converter.convert(new PaymentInspectRequest().payment(payment))
                .getPayment().getInvoice().isSetClientIdentity());
        payment.setCustomer(null);
        assertFalse(converter.convert(new PaymentInspectRequest().payment(payment))
                .getPayment().getInvoice().isSetClientIdentity());
    }

    @Test
    void missingCustomerIdIsValidatedByProtocol() {
        var converter = new PaymentInspectRequestToContextConverter(new CachToInternalDtoConverter());
        var payment = ApiBeanGenerator.initPayment();
        payment.setCustomer(new dev.vality.swag.fraudbusters.model.Customer().userId("Synthetic-User"));
        var identity = converter.convert(new PaymentInspectRequest().payment(payment))
                .getPayment().getInvoice().getClientIdentity();

        assertEquals("Synthetic-User", identity.getUserId());
        assertThrows(TProtocolException.class, () -> new TSerializer().serialize(identity));
    }

    @Test
    void convertTest() throws TException {
        PaymentInspectRequestToContextConverter paymentInspectRequestToContextConverter =
                new PaymentInspectRequestToContextConverter(new CachToInternalDtoConverter());

        Context context = paymentInspectRequestToContextConverter.convert(new PaymentInspectRequest()
                .payment(ApiBeanGenerator.initPayment()));

        assertNotNull(context);
        context.validate();
        context.getPayment().validate();
        context.getPayment().getPayment().validate();
        context.getPayment().getPayment().getCost().validate();
        context.getPayment().getPayment().getPayer().getPaymentResource().validate();
        context.getPayment().getPayment().getPayer().getPaymentResource().getContactInfo().validate();
        context.getPayment().getPayment().getPayer().getPaymentResource().getResource().validate();
        context.getPayment().getPayment().getPayer().getPaymentResource().getResource().getClientInfo().validate();
        context.getPayment().getPayment().getPayer().getPaymentResource().getResource().getPaymentTool().getBankCard()
                .validate();
        context.getPayment().getParty().validate();
        context.getPayment().getShop().validate();
        context.getPayment().getInvoice().validate();
    }

}
