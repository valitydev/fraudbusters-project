package dev.vality.trusted.tokens.initializer;

import dev.vality.trusted.tokens.constants.StreamType;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventStreamsPool {

    @Value("${spring.kafka.streams.properties.clean.timeout.sec}")
    private Long cleanTimeoutSec;

    private final Map<StreamType, KafkaStreams> kafkaStreamsList = new ConcurrentHashMap<>();

    public void put(StreamType type, KafkaStreams kafkaStreams) {
        kafkaStreamsList.put(type, kafkaStreams);
    }

    public KafkaStreams get(StreamType type) {
        return kafkaStreamsList.get(type);
    }

    public void cleanAll() {
        kafkaStreamsList.forEach((key, value) -> value.close(Duration.ofSeconds(cleanTimeoutSec)));
    }

}
