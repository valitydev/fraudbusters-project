package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.proxy_inspector.Context;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import dev.vality.swag.fraudbusters.model.PaymentInspectRequest;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentInspectRequestToContextConverterTest {

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
