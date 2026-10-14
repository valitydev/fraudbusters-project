package dev.vality.fraudbusters.mg.connector.mapper.impl;

import dev.vality.damsel.domain.Payer;
import dev.vality.damsel.fraudbusters.PayerType;
import dev.vality.damsel.fraudbusters.Refund;
import dev.vality.damsel.fraudbusters.RefundStatus;
import dev.vality.damsel.payment_processing.*;
import dev.vality.fraudbusters.mg.connector.constant.InvoiceEventType;
import dev.vality.fraudbusters.mg.connector.domain.InvoicePaymentWrapper;
import dev.vality.fraudbusters.mg.connector.mapper.Mapper;
import dev.vality.fraudbusters.mg.connector.mapper.initializer.InfoInitializer;
import dev.vality.fraudbusters.mg.connector.service.HgClientService;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.BiFunction;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundPaymentMapper implements Mapper<InvoiceChange, MachineEvent, Refund> {

    private final HgClientService hgClientService;
    private final InfoInitializer<InvoicePaymentRefundStatusChanged> generalInfoInitiator;

    @Override
    public boolean accept(InvoiceChange change) {
        return getChangeType().getFilter().match(change)
                && (change.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange()
                .getPayload().getInvoicePaymentRefundStatusChanged().getStatus().isSetFailed()
                || change.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange()
                .getPayload().getInvoicePaymentRefundStatusChanged().getStatus().isSetSucceeded());
    }

    @Override
    public Refund map(InvoiceChange change, MachineEvent event) {
        log.debug("RefundPaymentMapper change: {} event: {}", change, event);

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String paymentId = invoicePaymentChange.getId();
        InvoicePaymentRefundChange invoicePaymentRefundChange =
                invoicePaymentChange.getPayload().getInvoicePaymentRefundChange();
        InvoicePaymentRefundChangePayload payload = invoicePaymentRefundChange.getPayload();
        InvoicePaymentRefundStatusChanged invoicePaymentRefundStatusChanged =
                payload.getInvoicePaymentRefundStatusChanged();
        String refundId = invoicePaymentRefundChange.getId();

        InvoicePaymentWrapper invoicePaymentWrapper = hgClientService.getInvoiceInfo(event.getSourceId(), findPayment(),
                paymentId, refundId, event.getEventId());

        var invoice = invoicePaymentWrapper.getInvoice();
        var invoicePayment = invoicePaymentWrapper.getInvoicePayment();

        Payer payer = invoicePayment.getPayment().getPayer();

        Refund refund = new Refund()
                .setStatus(TBaseUtil.unionFieldToEnum(payload.getInvoicePaymentRefundStatusChanged().getStatus(),
                        RefundStatus.class))
                .setCost(invoicePayment.getPayment().getCost())
                .setReferenceInfo(generalInfoInitiator.initReferenceInfo(invoice))
                .setPaymentTool(generalInfoInitiator.initPaymentTool(payer))
                .setId(String.join(DELIMITER, invoice.getId(), invoicePayment.getPayment().getId(),
                        invoicePaymentRefundChange.getId()))
                .setPaymentId(String.join(DELIMITER, invoice.getId(), invoicePayment.getPayment().getId()))
                .setEventTime(event.getCreatedAt())
                .setClientInfo(generalInfoInitiator.initClientInfo(payer))
                .setProviderInfo(generalInfoInitiator.initProviderInfo(invoicePayment))
                .setPayerType(TBaseUtil.unionFieldToEnum(payer, PayerType.class))
                .setError(generalInfoInitiator.initError(invoicePaymentRefundStatusChanged));

        log.debug("RefundPaymentMapper refund: {}", refund);
        return refund;
    }

    private BiFunction<String, Invoice, Optional<InvoicePayment>> findPayment() {
        return (id, invoiceInfo) -> invoiceInfo.getPayments().stream()
                .filter(payment ->
                        payment.isSetPayment()
                                && payment.isSetRefunds()
                                && payment.getRefunds().stream()
                                .anyMatch(refund -> refund.getRefund().getId().equals(id))
                )
                .findFirst();
    }

    @Override
    public InvoiceEventType getChangeType() {
        return InvoiceEventType.INVOICE_PAYMENT_REFUND_STATUS_CHANGED;
    }

}
