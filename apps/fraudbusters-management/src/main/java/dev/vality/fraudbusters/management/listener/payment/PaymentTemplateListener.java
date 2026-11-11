package dev.vality.fraudbusters.management.listener.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.management.listener.ConfigurationCommandListener;
import dev.vality.fraudbusters.management.listener.InspectStorage;
import dev.vality.fraudbusters.management.service.iface.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentTemplateListener extends ConfigurationCommandListener {

    public PaymentTemplateListener(InspectStorage paymentInspectStorage, AuditService auditService) {
        super(paymentInspectStorage, auditService);
    }

    @KafkaListener(topics = "${kafka.topic.fraudbusters.payment.template}",
            containerFactory = "kafkaTemplateListenerContainerFactory")
    public void listen(Command command) {
        log.info("PaymentTemplateListener event: {}", command);
        applyCommandToProjection(command, InspectStorage::templates);
    }

}
