package dev.vality.fraudbusters.listener.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.listener.CommandListener;
import dev.vality.fraudbusters.pool.handler.GroupCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupListener implements CommandListener {

    private final GroupCommandHandler paymentGroupCommandHandler;

    @Override
    @KafkaListener(topics = "${kafka.topic.group-list}", containerFactory = "groupListenerContainerFactory")
    public void listen(@Payload Command command) {
        log.info("GroupListener command: {}", command);
        paymentGroupCommandHandler.handle(command);
    }

}
