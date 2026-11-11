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
public class GroupReferenceListener extends ConfigurationCommandListener {

    public GroupReferenceListener(InspectStorage paymentInspectStorage, AuditService auditService) {
        super(paymentInspectStorage, auditService);
    }

    @KafkaListener(topics = "${kafka.topic.fraudbusters.payment.group.reference}",
            containerFactory = "kafkaGroupReferenceListenerContainerFactory")
    public void listen(Command command) {
        log.info("GroupReferenceListener command: {}", command);
        if (command.getCommandBody().isSetGroupReference()) {
            applyCommandToProjection(command, InspectStorage::groupReferences);
        } else {
            log.warn("Unknown reference in command in ReferenceListener! command: {}", command);
        }
    }

}
