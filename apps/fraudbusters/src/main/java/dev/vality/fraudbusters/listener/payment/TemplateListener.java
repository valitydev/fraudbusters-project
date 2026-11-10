package dev.vality.fraudbusters.listener.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.listener.CommandListener;
import dev.vality.fraudbusters.pool.handler.TemplateCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateListener implements CommandListener {

    private final TemplateCommandHandler paymentTemplateCommandHandler;

    @Override
    @KafkaListener(topics = "${kafka.topic.template}", containerFactory = "templateListenerContainerFactory")
    public void listen(@Payload Command command) {
        log.info("TemplateListener command: {}", command);
        paymentTemplateCommandHandler.handle(command);
    }

}
