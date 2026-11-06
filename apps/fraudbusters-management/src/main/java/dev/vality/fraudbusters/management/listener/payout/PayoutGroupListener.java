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
public class PayoutGroupListener extends ConfigurationCommandListener {

    public PayoutGroupListener(InspectStorage payoutInspectStorage, AuditService auditService) {
        super(payoutInspectStorage, auditService);
    }

    @KafkaListener(topics = "${kafka.topic.fraudbusters.payout.group.list}",
            containerFactory = "kafkaGroupListenerContainerFactory")
    public void listen(Command command) {
        log.info("PayoutGroupListener event: {}", command);
        apply(command, InspectStorage::groups);
    }

}
