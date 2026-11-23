package dev.vality.fraudbusters.listener.events.clickhouse;

import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.fraudbusters.config.service.ListenersConfigurationService;
import dev.vality.fraudbusters.fraud.localstorage.LocalResultStorage;
import dev.vality.fraudbusters.repository.Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutEventListener {

    private final Repository<Payout> repository;

    private final LocalResultStorage localResultStorage;

    @KafkaListener(topics = "${kafka.topic.event.sink.payout}",
            containerFactory = "kafkaPayoutResultListenerContainerFactory")
    public void listen(
            List<Payout> payouts,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) throws InterruptedException {
        try {
            log.info("Listen payouts size: {} partition: {} offset: {}", payouts.size(), partition, offset);
            log.debug("Listen payouts: {}", payouts);
            repository.insertBatch(payouts);
            localResultStorage.clear();
        } catch (Exception e) {
            log.warn("Error when payouts listen e: ", e);
            Thread.sleep(ListenersConfigurationService.THROTTLING_TIMEOUT);
            throw e;
        }
    }

}
