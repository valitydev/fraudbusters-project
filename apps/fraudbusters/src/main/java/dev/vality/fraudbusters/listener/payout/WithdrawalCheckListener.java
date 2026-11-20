package dev.vality.fraudbusters.listener.payout;

import dev.vality.fraudbusters.config.service.ListenersConfigurationService;
import dev.vality.fraudbusters.domain.PayoutResult;
import dev.vality.fraudbusters.repository.Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalCheckListener {

    private final Repository<PayoutResult> repository;

    @KafkaListener(topics = "${kafka.topic.payout-result}",
            containerFactory = "kafkaWithdrawalCheckListenerContainerFactory")
    public void listen(
            List<PayoutResult> batch, @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) throws InterruptedException {
        try {
            log.info("WithdrawalCheckListener listen result size: {} partition: {} offset: {}",
                    batch.size(), partition, offset
            );
            repository.insertBatch(batch.stream()
                    .filter(Objects::nonNull)
                    .toList());
        } catch (Exception e) {
            log.warn("Error when WithdrawalCheckListener listen e: ", e);
            Thread.sleep(ListenersConfigurationService.THROTTLING_TIMEOUT);
            throw e;
        }
    }
}
