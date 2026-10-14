package dev.vality.fraudbusters.management.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestKafkaProducer<T> {

    private final KafkaTemplate<String, T> kafkaTemplate;

    public void send(String topic, T payload) {
        log.info("Sending payload='{}' to topic='{}'", payload, topic);
        try {
            kafkaTemplate.send(topic, payload).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        kafkaTemplate.getProducerFactory().reset();
    }

    public void send(String topic, String key, T payload) {
        log.info("Sending key='{}' payload='{}' to topic='{}'", key, payload, topic);
        try {
            kafkaTemplate.send(topic, key, payload).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        kafkaTemplate.getProducerFactory().reset();
    }

}