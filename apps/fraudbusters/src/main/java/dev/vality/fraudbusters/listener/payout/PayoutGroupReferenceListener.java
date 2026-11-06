package dev.vality.fraudbusters.listener.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.listener.GroupReferenceCommandListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PayoutGroupReferenceListener extends GroupReferenceCommandListener {

    public PayoutGroupReferenceListener(InspectStorage payoutInspectStorage) {
        super(payoutInspectStorage);
    }

    @Override
    @KafkaListener(topics = "${kafka.topic.payout-group-reference}",
            containerFactory = "payoutGroupReferenceListenerContainerFactory")
    public void listen(@Payload Command command) {
        handle(command);
    }

}
