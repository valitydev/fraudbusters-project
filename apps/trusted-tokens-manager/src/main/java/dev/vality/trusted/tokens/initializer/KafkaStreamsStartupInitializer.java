package dev.vality.trusted.tokens.initializer;

import dev.vality.trusted.tokens.factory.EventFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaStreamsStartupInitializer {

    private final List<EventFactory> eventFactories;
    private final EventStreamsPool eventStreamsPool;

    @Value("${spring.kafka.streams.properties.clean.timeout.sec}")
    private Long cleanTimeoutSec;

    @EventListener(value = ContextRefreshedEvent.class)
    @Order
    public void onRefresh() {
        if (!CollectionUtils.isEmpty(eventFactories)) {
            eventFactories.forEach(this::initKafkaStream);
        }
    }

    private void initKafkaStream(EventFactory eventFactory) {
        KafkaStreams kafkaStreams = eventFactory.create();
        kafkaStreams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> kafkaStreams.close(Duration.ofSeconds(cleanTimeoutSec))));
        eventStreamsPool.put(eventFactory.getType(), kafkaStreams);
        log.info("KafkaStreamsStartupInitializer start {} stream kafkaStreams: {}", eventFactory.getType().name(),
                kafkaStreams.metadataForLocalThreads());
    }
}
