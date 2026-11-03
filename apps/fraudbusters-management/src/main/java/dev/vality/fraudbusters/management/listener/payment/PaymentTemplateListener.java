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
public class PaymentTemplateListener extends CommandListener {

    private final InspectStorageRegistry inspectStorageRegistry;
    private final AuditService auditService;

    @KafkaListener(topics = "${kafka.topic.fraudbusters.payment.template}",
            containerFactory = "kafkaTemplateListenerContainerFactory")
    public void listen(Command command) {
        log.info("PaymentTemplateListener event: {}", command);
        handle(command, inspectStorageRegistry.get(command).templates());
        auditService.logCommand(command);
    }

}
