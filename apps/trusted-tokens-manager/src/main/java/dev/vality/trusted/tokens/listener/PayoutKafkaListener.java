package dev.vality.trusted.tokens.listener;

import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.kafka.common.util.LogUtil;
import dev.vality.trusted.tokens.exception.TransactionSavingException;
import dev.vality.trusted.tokens.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutKafkaListener {

    private final PayoutService payoutService;

    @Value("${spring.kafka.consumer.properties.throttling.timeout.ms}")
    private int throttlingTimeout;

    @KafkaListener(
            autoStartup = "${kafka.topics.payout.consume.enabled}",
            topics = "${kafka.topics.payout.dest}",
            containerFactory = "payoutListenerContainerFactory")
    public void listen(
            List<ConsumerRecord<String, Payout>> batch,
            Acknowledgment ack) {
        try {
            log.info("PayoutKafkaListener listen offsets, size={}, {}",
                    batch.size(), toSummaryPayoutString(batch));
            List<Payout> payouts = batch.stream()
                    .map(ConsumerRecord::value)
                    .collect(Collectors.toList());
            payoutService.saveAll(payouts);
            ack.acknowledge();
            log.info("PayoutKafkaListener Records have been committed, size={}, {}",
                    batch.size(), toSummaryPayoutString(batch));
        } catch (TransactionSavingException ex) {
            log.error("Error when PayoutKafkaListener listen ex,", ex);
            ack.nack(ex.getTrxIndex(), Duration.ofMillis(throttlingTimeout));
            throw ex;
        }
    }

    public static <K> String toSummaryPayoutString(List<ConsumerRecord<K, Payout>> records) {
        String valueKeysString = records.stream().map(ConsumerRecord::value)
                .map((value) -> String.format("'%s' - '%s'", value.getId(), value.getStatus()))
                .collect(Collectors.joining(", "));
        return String.format("%s, values={%s}", LogUtil.toSummaryString(records), valueKeysString);
    }
}
