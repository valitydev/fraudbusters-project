package dev.vality.fraudbusters.listener.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.listener.TemplateReferenceCommandListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class TemplateReferenceListener extends TemplateReferenceCommandListener {

    public TemplateReferenceListener(InspectStorage paymentInspectStorage) {
        super(paymentInspectStorage);
    }

    @Override
    @KafkaListener(topics = "${kafka.topic.reference}", containerFactory = "referenceListenerContainerFactory")
    public void listen(@Payload Command command) {
        handle(command);
    }

}
