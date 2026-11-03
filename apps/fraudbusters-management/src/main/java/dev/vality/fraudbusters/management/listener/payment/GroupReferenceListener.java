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
public class GroupReferenceListener extends CommandListener {

    private final InspectStorageRegistry inspectStorageRegistry;
    private final AuditService auditService;

    @KafkaListener(topics = "${kafka.topic.fraudbusters.payment.group.reference}",
            containerFactory = "kafkaGroupReferenceListenerContainerFactory")
    public void listen(Command command) {
        log.info("GroupReferenceListener command: {}", command);
        if (command.getCommandBody().isSetGroupReference()) {
            handle(command, inspectStorageRegistry.get(command).groupReferences());
            auditService.logCommand(command);
        } else {
            log.warn("Unknown reference in command in ReferenceListener! command: {}", command);
        }
    }
}
