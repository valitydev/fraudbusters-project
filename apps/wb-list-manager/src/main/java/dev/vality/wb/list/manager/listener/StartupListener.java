package dev.vality.wb.list.manager.listener;

import dev.vality.wb.list.manager.config.properties.WbListCorrectionStreamProperties;
import dev.vality.wb.list.manager.stream.WbListErrorRowsCorrectionStreamFactory;
import dev.vality.wb.list.manager.stream.WbListStreamFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    private final WbListStreamFactory wbListStreamFactory;
    private final Properties wbListStreamProperties;
    private final KafkaListenerEndpointRegistry registry;
    private final WbListErrorRowsCorrectionStreamFactory wbListErrorRowsCorrectionStreamFactory;
    private final WbListCorrectionStreamProperties wbListCorrectionStreamProperties;

    private KafkaStreams kafkaStreams;
    private KafkaStreams kafkaStreamsWbListErrorRowsCorrection;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        kafkaStreams = wbListStreamFactory.create(wbListStreamProperties);
        kafkaStreams.start();
        log.info("StartupListener start stream kafkaStreams: {}", kafkaStreams.metadataForAllStreamsClients());

        if (wbListCorrectionStreamProperties.getEnabled()) {
            Properties properties = new Properties();
            properties.putAll(wbListStreamProperties);
            properties.put("application.id", wbListCorrectionStreamProperties.getApplicationId());
            properties.put("client.id", wbListCorrectionStreamProperties.getClientId());
            kafkaStreamsWbListErrorRowsCorrection =
                    wbListErrorRowsCorrectionStreamFactory.create(properties);
            kafkaStreamsWbListErrorRowsCorrection.start();
            log.info("StartupListener start stream kafkaStreamsWbListErrorRowsCorrection: {}",
                    kafkaStreamsWbListErrorRowsCorrection.metadataForAllStreamsClients());
        }
    }

    public void stop() {
        kafkaStreams.close(Duration.ofSeconds(1L));
        if (wbListCorrectionStreamProperties.getEnabled()) {
            kafkaStreamsWbListErrorRowsCorrection.close(Duration.ofSeconds(1L));
        }
        registry.stop();
    }

}