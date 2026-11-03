package dev.vality.fraudbusters.listener.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.listener.GroupReferenceCommandListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class GroupReferenceListener extends GroupReferenceCommandListener {

    public GroupReferenceListener(InspectStorage paymentInspectStorage) {
        super(paymentInspectStorage);
    }

    @Override
    @KafkaListener(topics = "${kafka.topic.group-reference}",
            containerFactory = "groupReferenceListenerContainerFactory")
    public void listen(@Payload Command command) {
        handle(command);
    }

}
