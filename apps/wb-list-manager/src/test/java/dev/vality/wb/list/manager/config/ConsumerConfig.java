package dev.vality.wb.list.manager.config;

import dev.vality.damsel.wb_list.Event;
import dev.vality.testcontainers.annotations.kafka.config.KafkaConsumer;
import dev.vality.wb.list.manager.serializer.EventDeserializer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaConsumer<Event> testEventKafkaConsumer() {
        return new KafkaConsumer<>(bootstrapServers, new EventDeserializer());
    }
}
