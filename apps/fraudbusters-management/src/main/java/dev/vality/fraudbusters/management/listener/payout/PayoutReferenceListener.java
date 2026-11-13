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
public class PayoutReferenceListener extends ConfigurationCommandListener {

    public PayoutReferenceListener(InspectStorage payoutInspectStorage, AuditService auditService) {
        super(payoutInspectStorage, auditService);
    }

    @KafkaListener(topics = "#{payoutProperties.referenceTopic}",
            containerFactory = "kafkaReferenceListenerContainerFactory")
    public void listen(Command command) {
        log.info("PayoutReferenceListener command: {}", command);
        if (command.getCommandBody().isSetReference()) {
            applyCommandToProjection(command, InspectStorage::references);
        } else {
            log.warn("Unknown reference in command in PayoutReferenceListener! command: {}", command);
        }
    }

}
