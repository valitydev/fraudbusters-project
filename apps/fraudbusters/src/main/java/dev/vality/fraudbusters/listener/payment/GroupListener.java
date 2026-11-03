package dev.vality.fraudbusters.listener.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.listener.GroupCommandListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class GroupListener extends GroupCommandListener {

    public GroupListener(InspectStorage paymentInspectStorage) {
        super(paymentInspectStorage);
    }

    @Override
    @KafkaListener(topics = "${kafka.topic.group-list}", containerFactory = "groupListenerContainerFactory")
    public void listen(@Payload Command command) {
        handle(command);
    }

}
