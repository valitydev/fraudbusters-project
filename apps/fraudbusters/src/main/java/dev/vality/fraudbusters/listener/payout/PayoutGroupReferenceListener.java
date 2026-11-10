package dev.vality.fraudbusters.listener.payout;

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
public class PayoutGroupReferenceListener implements CommandListener {

    private final GroupReferenceCommandHandler payoutGroupReferenceCommandHandler;

    @Override
    @KafkaListener(topics = "${kafka.topic.payout-group-reference}",
            containerFactory = "payoutGroupReferenceListenerContainerFactory")
    public void listen(@Payload Command command) {
        log.info("PayoutGroupReferenceListener command: {}", command);
        payoutGroupReferenceCommandHandler.handle(command);
    }

}
