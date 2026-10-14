package dev.vality.fraudbusters.mg.connector.service;

import dev.vality.damsel.payment_processing.Invoice;
import dev.vality.damsel.payment_processing.InvoicePayment;
import dev.vality.damsel.payment_processing.InvoicingSrv;
import dev.vality.fraudbusters.mg.connector.domain.InvoicePaymentWrapper;
import dev.vality.fraudbusters.mg.connector.exception.PaymentInfoNotFoundException;
import dev.vality.fraudbusters.mg.connector.exception.PaymentInfoVersionNotFoundException;
import dev.vality.fraudbusters.mg.connector.factory.EventRangeFactory;
import dev.vality.woody.api.flow.error.WUnavailableResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.NetworkException;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.BiFunction;

@Slf4j
@Service
@RequiredArgsConstructor
public class HgClientService {

    private final InvoicingSrv.Iface invoicingClient;
    private final EventRangeFactory eventRangeFactory;

    public InvoicePaymentWrapper getInvoiceInfo(
            String invoiceId,
            BiFunction<String, Invoice, Optional<InvoicePayment>> findPaymentPredicate,
            String paymentId,
            String eventId,
            long sequenceId) {
        return getInvoiceFromHg(invoiceId, findPaymentPredicate, eventId, sequenceId);
    }

    public InvoicePaymentWrapper getInvoiceInfo(
            String invoiceId,
            BiFunction<String, Invoice, Optional<InvoicePayment>> findPaymentPredicate,
            String paymentId,
            long sequenceId) {
        return getInvoiceFromHg(invoiceId, findPaymentPredicate, paymentId, sequenceId);
    }

    private InvoicePaymentWrapper getInvoiceFromHg(
            String invoiceId,
            BiFunction<String, Invoice, Optional<InvoicePayment>> findPaymentPredicate,
            String eventId,
            long sequenceId) {
        InvoicePaymentWrapper invoicePaymentWrapper = new InvoicePaymentWrapper();
        try {
            log.info("invoiceId: {} sequenceId: {}", invoiceId, sequenceId);
            Invoice invoiceInfo = invoicingClient.get(invoiceId, eventRangeFactory.create(sequenceId));
            if (invoiceInfo == null) {
                throw new PaymentInfoNotFoundException("Not found invoice info in hg!");
            } else if (invoiceInfo.getLatestEventId() != sequenceId) {
                throw new PaymentInfoVersionNotFoundException(
                        String.format("Not found invoice info in hg wait: %s come: %s", sequenceId,
                                invoiceInfo.getLatestEventId()));
            }
            invoicePaymentWrapper.setInvoice(invoiceInfo.getInvoice());
            findPaymentPredicate.apply(eventId, invoiceInfo)
                    .ifPresentOrElse(invoicePaymentWrapper::setInvoicePayment, () -> {
                        throw new PaymentInfoNotFoundException("Not found payment in invoice!");
                    });
            return invoicePaymentWrapper;
        } catch (WUnavailableResultException | TException e) {
            log.error("Error when HgClientService getInvoiceInfo invoiceId: {} eventId: {} sequenceId: {} e: ",
                    invoiceId, eventId, sequenceId, e);
            throw new NetworkException(e);
        }
    }
}
