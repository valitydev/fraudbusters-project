package dev.vality.fraudbusters.listener.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.listener.GroupCommandListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PayoutGroupListener extends GroupCommandListener {

    public PayoutGroupListener(InspectStorage payoutInspectStorage) {
        super(payoutInspectStorage);
    }

    @Override
    @KafkaListener(topics = "${kafka.topic.payout-group-list}",
            containerFactory = "payoutGroupListenerContainerFactory")
    public void listen(@Payload Command command) {
        handle(command);
    }

}
