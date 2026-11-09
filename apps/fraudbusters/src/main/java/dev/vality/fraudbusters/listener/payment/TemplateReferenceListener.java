package dev.vality.fraudbusters.listener.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.listener.CommandListener;
import dev.vality.fraudbusters.pool.handler.TemplateReferenceCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateReferenceListener implements CommandListener {

    private final TemplateReferenceCommandHandler paymentTemplateReferenceCommandHandler;

    @Override
    @KafkaListener(topics = "${kafka.topic.reference}", containerFactory = "referenceListenerContainerFactory")
    public void listen(@Payload Command command) {
        log.info("TemplateReferenceListener command: {}", command);
        paymentTemplateReferenceCommandHandler.handle(command);
    }

}
