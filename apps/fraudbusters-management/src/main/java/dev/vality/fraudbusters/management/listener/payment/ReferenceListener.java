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
public class ReferenceListener extends ConfigurationCommandListener {

    public ReferenceListener(InspectStorage paymentInspectStorage, AuditService auditService) {
        super(paymentInspectStorage, auditService);
    }

    @KafkaListener(topics = "${kafka.topic.fraudbusters.payment.reference}",
            containerFactory = "kafkaReferenceListenerContainerFactory")
    public void listen(Command command) {
        log.info("ReferenceListener command: {}", command);
        if (command.getCommandBody().isSetReference()) {
            applyCommandToProjection(command, InspectStorage::references);
        } else {
            log.warn("Unknown reference in command in ReferenceListener! command: {}", command);
        }
    }

}
