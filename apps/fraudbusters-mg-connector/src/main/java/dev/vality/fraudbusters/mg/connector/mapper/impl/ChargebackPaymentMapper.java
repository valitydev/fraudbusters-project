package dev.vality.fraudbusters.mg.connector.mapper.impl;

import dev.vality.damsel.domain.Payer;
import dev.vality.damsel.fraudbusters.Chargeback;
import dev.vality.damsel.fraudbusters.ChargebackCategory;
import dev.vality.damsel.fraudbusters.ChargebackStatus;
import dev.vality.damsel.fraudbusters.PayerType;
import dev.vality.damsel.payment_processing.*;
import dev.vality.fraudbusters.mg.connector.constant.InvoiceEventType;
import dev.vality.fraudbusters.mg.connector.domain.InvoicePaymentWrapper;
import dev.vality.fraudbusters.mg.connector.mapper.Mapper;
import dev.vality.fraudbusters.mg.connector.mapper.initializer.GeneralInfoInitiator;
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
public class ChargebackPaymentMapper implements Mapper<InvoiceChange, MachineEvent, Chargeback> {

    private final HgClientService hgClientService;
    private final GeneralInfoInitiator generalInfoInitiator;

    @Override
    public boolean accept(InvoiceChange change) {
        return getChangeType().getFilter().match(change)
                && (change.getInvoicePaymentChange().getPayload().getInvoicePaymentChargebackChange()
                .getPayload().getInvoicePaymentChargebackStatusChanged().getStatus().isSetRejected()
                || change.getInvoicePaymentChange().getPayload().getInvoicePaymentChargebackChange()
                .getPayload().getInvoicePaymentChargebackStatusChanged().getStatus().isSetAccepted()
                || change.getInvoicePaymentChange().getPayload().getInvoicePaymentChargebackChange()
                .getPayload().getInvoicePaymentChargebackStatusChanged().getStatus().isSetCancelled());
    }

    @Override
    public Chargeback map(InvoiceChange change, MachineEvent event) {
        log.debug("ChargebackPaymentMapper change: {} event: {}", change, event);

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String paymentId = invoicePaymentChange.getId();
        InvoicePaymentChargebackChange invoicePaymentChargebackChange =
                invoicePaymentChange.getPayload().getInvoicePaymentChargebackChange();
        InvoicePaymentChargebackChangePayload payload = invoicePaymentChargebackChange.getPayload();
        InvoicePaymentChargebackStatusChanged invoicePaymentChargebackStatusChanged =
                payload.getInvoicePaymentChargebackStatusChanged();

        String chargebackId = invoicePaymentChargebackChange.getId();
        InvoicePaymentWrapper invoicePaymentWrapper = hgClientService.getInvoiceInfo(event.getSourceId(), findPayment(),
                paymentId, chargebackId, event.getEventId());

        var invoice = invoicePaymentWrapper.getInvoice();
        var invoicePayment = invoicePaymentWrapper.getInvoicePayment();

        Payer payer = invoicePayment.getPayment().getPayer();

        Chargeback chargeback = new Chargeback()
                .setStatus(TBaseUtil
                        .unionFieldToEnum(invoicePaymentChargebackStatusChanged.getStatus(), ChargebackStatus.class))
                .setCost(invoicePayment.getPayment().getCost())
                .setReferenceInfo(generalInfoInitiator.initReferenceInfo(invoice))
                .setPaymentTool(generalInfoInitiator.initPaymentTool(payer))
                .setId(String.join(DELIMITER, invoice.getId(), invoicePayment.getPayment().getId(),
                        invoicePaymentChargebackChange.getId()))
                .setPaymentId(String.join(DELIMITER, invoice.getId(), invoicePayment.getPayment().getId()))
                .setEventTime(event.getCreatedAt())
                .setClientInfo(generalInfoInitiator.initClientInfo(payer))
                .setPayerType(TBaseUtil.unionFieldToEnum(payer, PayerType.class))
                .setProviderInfo(generalInfoInitiator.initProviderInfo(invoicePayment));

        invoicePayment.getChargebacks().stream()
                .filter(chargebackVal -> chargebackVal.getChargeback().getId().equals(chargebackId))
                .findFirst()
                .ifPresent(paymentChargeback -> {
                    dev.vality.damsel.domain.InvoicePaymentChargeback invoicePaymentChargeback =
                            paymentChargeback.getChargeback();
                    chargeback.setChargebackCode(invoicePaymentChargeback.getReason().getCode() != null
                            ? invoicePaymentChargeback.getReason().getCode()
                            : GeneralInfoInitiator.UNKNOWN)
                            .setCategory(TBaseUtil.unionFieldToEnum(invoicePaymentChargeback.getReason().getCategory(),
                                    ChargebackCategory.class));
                });

        log.debug("ChargebackPaymentMapper chargebackRow: {}", chargeback);
        return chargeback;
    }

    private BiFunction<String, Invoice, Optional<InvoicePayment>> findPayment() {
        return (id, invoiceInfo) -> invoiceInfo.getPayments().stream()
                .filter(payment -> payment.isSetPayment()
                        && payment.isSetChargebacks()
                        && payment.getChargebacks().stream()
                        .anyMatch(chargeback -> chargeback.getChargeback().getId().equals(id))
                )
                .findFirst();
    }

    @Override
    public InvoiceEventType getChangeType() {
        return InvoiceEventType.INVOICE_PAYMENT_CHARGEBACK_STATUS_CHANGED;
    }

}
