package dev.vality.fraudbusters.management.config;

import dev.vality.damsel.wb_list.ChangeCommand;
import dev.vality.fraudbusters.management.serializer.CommandChangeDeserializer;
import dev.vality.testcontainers.annotations.kafka.config.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaConsumer<ChangeCommand> testChangeCommandKafkaConsumer() {
        return new KafkaConsumer<>(bootstrapServers, new CommandChangeDeserializer());
    }
}
