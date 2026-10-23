package dev.vality.fraudbusters.mg.connector.mapper.initializer;

import dev.vality.damsel.domain.ClientIdentity;
import dev.vality.damsel.domain.Invoice;
import dev.vality.damsel.domain.Payer;
import dev.vality.damsel.domain.PaymentResourcePayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientIdentityTest {

    @Test
    void invoiceIdentitySurvivesGeneralAndRefundPaths() {
        var general = new GeneralInfoInitiator();
        var refund = new RefundInfoInitiatorDecorator(general);
        var payer = Payer.payment_resource(new PaymentResourcePayer());
        var invoice = new Invoice();
        assertFalse(general.initClientInfo(payer, invoice).isSetCustomerId());
        assertFalse(refund.initClientInfo(payer, invoice).isSetUserId());
        invoice.setClientIdentity(new ClientIdentity("synthetic-customer").setUserId("Synthetic-User"));
        for (var info : java.util.List.of(general.initClientInfo(payer, invoice),
                refund.initClientInfo(payer, invoice))) {
            assertEquals("synthetic-customer", info.getCustomerId());
            assertEquals("Synthetic-User", info.getUserId());
        }
        invoice.getClientIdentity().unsetUserId();
        assertFalse(refund.initClientInfo(payer, invoice).isSetUserId());
    }
}
