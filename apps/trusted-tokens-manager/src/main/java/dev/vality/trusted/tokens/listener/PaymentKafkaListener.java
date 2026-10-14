package dev.vality.trusted.tokens.listener;

import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.kafka.common.util.LogUtil;
import dev.vality.trusted.tokens.exception.TransactionSavingException;
import dev.vality.trusted.tokens.service.PaymentService;
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
public class PaymentKafkaListener {

    private final PaymentService paymentService;

    @Value("${spring.kafka.consumer.properties.throttling.timeout.ms}")
    private int throttlingTimeout;

    @KafkaListener(
            autoStartup = "${kafka.topics.payment.consume.enabled}",
            topics = "${kafka.topics.payment.dest}",
            containerFactory = "paymentListenerContainerFactory")
    public void listen(
            List<ConsumerRecord<String, Payment>> batch,
            Acknowledgment ack) {
        try {
            log.info("PaymentKafkaListener listen offsets, size={}, {}",
                    batch.size(), toSummaryPaymentString(batch));
            List<Payment> payments = batch.stream()
                    .map(ConsumerRecord::value)
                    .collect(Collectors.toList());
            paymentService.saveAll(payments);
            ack.acknowledge();
            log.info("PaymentKafkaListener Records have been committed, size={}, {}",
                    batch.size(), toSummaryPaymentString(batch));
        } catch (TransactionSavingException ex) {
            log.error("Error when PaymentKafkaListener listen ex,", ex);
            ack.nack(ex.getTrxIndex(), Duration.ofMillis(throttlingTimeout));
            throw ex;
        }
    }

    public static <K> String toSummaryPaymentString(List<ConsumerRecord<K, Payment>> records) {
        String valueKeysString = records.stream().map(ConsumerRecord::value)
                .map((value) -> String.format("'%s' - '%s'", value.getId(), value.getStatus()))
                .collect(Collectors.joining(", "));
        return String.format("%s, values={%s}", LogUtil.toSummaryString(records), valueKeysString);
    }
}
