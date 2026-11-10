package dev.vality.fraudbusters.listener.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.listener.CommandListener;
import dev.vality.fraudbusters.pool.handler.GroupReferenceCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupReferenceListener implements CommandListener {

    private final GroupReferenceCommandHandler paymentGroupReferenceCommandHandler;

    @Override
    @KafkaListener(topics = "${kafka.topic.group-reference}",
            containerFactory = "groupReferenceListenerContainerFactory")
    public void listen(@Payload Command command) {
        log.info("GroupReferenceListener command: {}", command);
        paymentGroupReferenceCommandHandler.handle(command);
    }

}
