package dev.vality.fraudbusters.mg.connector.mapper.initializer;

import dev.vality.damsel.domain.Invoice;
import dev.vality.damsel.domain.Payer;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.fraudbusters.ClientInfo;
import dev.vality.damsel.fraudbusters.Error;
import dev.vality.damsel.fraudbusters.ProviderInfo;
import dev.vality.damsel.fraudbusters.ReferenceInfo;
import dev.vality.damsel.payment_processing.InvoicePayment;

public interface InfoInitializer<T> {

    Error initError(T t);

    ClientInfo initClientInfo(Payer payer);

    void initContactInfo(ClientInfo clientInfo, Payer payer);

    ProviderInfo initProviderInfo(InvoicePayment invoicePayment);

    ReferenceInfo initReferenceInfo(Invoice invoice);

    PaymentTool initPaymentTool(Payer payer);

}
