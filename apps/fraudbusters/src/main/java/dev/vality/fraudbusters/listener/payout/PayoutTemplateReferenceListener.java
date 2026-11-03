package dev.vality.fraudbusters.listener.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.listener.TemplateReferenceCommandListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PayoutTemplateReferenceListener extends TemplateReferenceCommandListener {

    public PayoutTemplateReferenceListener(InspectStorage payoutInspectStorage) {
        super(payoutInspectStorage);
    }

    @Override
    @KafkaListener(topics = "${kafka.topic.payout-reference}",
            containerFactory = "payoutReferenceListenerContainerFactory")
    public void listen(@Payload Command command) {
        handle(command);
    }

}
