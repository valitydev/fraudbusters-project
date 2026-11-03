package dev.vality.fraudbusters.management.listener.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.management.listener.CommandListener;
import dev.vality.fraudbusters.management.listener.InspectStorageRegistry;
import dev.vality.fraudbusters.management.service.iface.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReferenceListener extends CommandListener {

    private final InspectStorageRegistry inspectStorageRegistry;
    private final AuditService auditService;

    @KafkaListener(topics = "${kafka.topic.fraudbusters.payment.reference}",
            containerFactory = "kafkaReferenceListenerContainerFactory")
    public void listen(Command command) {
        log.info("ReferenceListener command: {}", command);
        if (command.getCommandBody().isSetReference()) {
            handle(command, inspectStorageRegistry.get(command).references());
            auditService.logCommand(command);
        } else {
            log.warn("Unknown reference in command in ReferenceListener! command: {}", command);
        }
    }
}
