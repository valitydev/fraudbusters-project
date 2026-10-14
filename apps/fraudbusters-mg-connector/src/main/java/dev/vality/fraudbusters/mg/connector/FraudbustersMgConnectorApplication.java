package dev.vality.fraudbusters.mg.connector;

import dev.vality.fraudbusters.mg.connector.listener.StreamStateManager;
import dev.vality.fraudbusters.mg.connector.pool.EventSinkStreamsPool;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ServletComponentScan
@SpringBootApplication
public class FraudbustersMgConnectorApplication extends SpringApplication {

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private EventSinkStreamsPool eventSinkStreamsPool;

    @Autowired
    private StreamStateManager streamStateManager;

    public static void main(String[] args) {
        SpringApplication.run(FraudbustersMgConnectorApplication.class, args);
    }

    @PreDestroy
    public void preDestroy() {
        streamStateManager.stop();
        eventSinkStreamsPool.cleanAll();
        kafkaListenerEndpointRegistry.stop();
    }

}
