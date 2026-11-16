package dev.vality.fraudbusters.management.listener.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.management.listener.ConfigurationCommandListener;
import dev.vality.fraudbusters.management.listener.InspectStorage;
import dev.vality.fraudbusters.management.service.iface.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayoutGroupReferenceListener extends ConfigurationCommandListener {

    public PayoutGroupReferenceListener(InspectStorage payoutInspectStorage, AuditService auditService) {
        super(payoutInspectStorage, auditService);
    }

    @KafkaListener(topics = "#{payoutProperties.groupReferenceTopic}",
            containerFactory = "kafkaGroupReferenceListenerContainerFactory")
    public void listen(Command command) {
        log.info("PayoutGroupReferenceListener command: {}", command);
        if (command.getCommandBody().isSetGroupReference()) {
            applyCommandToProjection(command, InspectStorage::groupReferences);
        } else {
            log.warn("Unknown reference in command in PayoutGroupReferenceListener! command: {}", command);
        }
    }

}
