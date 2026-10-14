package dev.vality.trusted.tokens.listener;

import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.kafka.common.util.LogUtil;
import dev.vality.trusted.tokens.exception.TransactionSavingException;
import dev.vality.trusted.tokens.service.WithdrawalService;
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
public class WithdrawalKafkaListener {

    private final WithdrawalService withdrawalService;

    @Value("${spring.kafka.consumer.properties.throttling.timeout.ms}")
    private int throttlingTimeout;

    @KafkaListener(
            autoStartup = "${kafka.topics.withdrawal.consume.enabled}",
            topics = "${kafka.topics.withdrawal.dest}",
            containerFactory = "withdrawalListenerContainerFactory")
    public void listen(
            List<ConsumerRecord<String, Withdrawal>> batch,
            Acknowledgment ack) {
        try {
            log.info("WithdrawalKafkaListener listen offsets, size={}, {}",
                    batch.size(), toSummaryWithdrawalString(batch));
            List<Withdrawal> withdrawals = batch.stream()
                    .map(ConsumerRecord::value)
                    .collect(Collectors.toList());
            withdrawalService.saveAll(withdrawals);
            ack.acknowledge();
            log.info("WithdrawalKafkaListener Records have been committed, size={}, {}",
                    batch.size(), toSummaryWithdrawalString(batch));
        } catch (TransactionSavingException ex) {
            log.error("Error when WithdrawalKafkaListener listen ex,", ex);
            ack.nack(ex.getTrxIndex(), Duration.ofMillis(throttlingTimeout));
            throw ex;
        }
    }

    public static <K> String toSummaryWithdrawalString(List<ConsumerRecord<K, Withdrawal>> records) {
        String valueKeysString = records.stream().map(ConsumerRecord::value)
                .map((value) -> String.format("'%s' - '%s'", value.getId(), value.getStatus()))
                .collect(Collectors.joining(", "));
        return String.format("%s, values={%s}", LogUtil.toSummaryString(records), valueKeysString);
    }
}
