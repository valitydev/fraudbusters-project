package dev.vality.fraudbusters.management.config;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.ReferenceInfo;
import dev.vality.damsel.wb_list.Event;
import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraudbusters.management.serializer.CommandFraudDeserializer;
import dev.vality.fraudbusters.management.serializer.EventDeserializer;
import dev.vality.fraudbusters.management.serializer.FraudDataCandidateDeserializer;
import dev.vality.fraudbusters.management.serializer.ReferenceInfoDeserializer;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.thrift.TBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("LineLength")
public class KafkaConfig {

    @Value("${kafka.consumer-group.wb-list}")
    private String consumerGroupWbList;

    private final KafkaProperties kafkaProperties;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    @Bean
    public ConsumerFactory<String, Event> consumerFactory() {
        Map<String, Object> configs = consumerConfigs();
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventDeserializer.class);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupWbList);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Event>> kafkaListenerContainerFactory(
            ConsumerFactory<String, Event> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Event> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Command> consumerTemplateFactory() {
        Map<String, Object> configs = consumerConfigs();
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CommandFraudDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Command>> kafkaTemplateListenerContainerFactory(
            ConsumerFactory<String, Command> consumerTemplateFactory) {
        return createDefaultContainerFactory(consumerTemplateFactory);
    }

    @Bean
    public ConsumerFactory<String, ReferenceInfo> consumerReferenceInfoFactory() {
        Map<String, Object> configs = consumerConfigs();
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ReferenceInfoDeserializer.class);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupWbList);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, ReferenceInfo>> kafkaReferenceInfoListenerContainerFactory(
            ConsumerFactory<String, ReferenceInfo> consumerReferenceInfoFactory) {
        ConcurrentKafkaListenerContainerFactory<String, ReferenceInfo> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerReferenceInfoFactory);
        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        return factory;
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Command>> kafkaGroupListenerContainerFactory(
            ConsumerFactory<String, Command> consumerTemplateFactory) {
        return createDefaultContainerFactory(consumerTemplateFactory);
    }

    @Bean
    public ConsumerFactory<String, Command> consumerReferenceFactory() {
        Map<String, Object> configs = consumerConfigs();
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CommandFraudDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Command>> kafkaReferenceListenerContainerFactory(
            ConsumerFactory<String, Command> consumerReferenceFactory) {
        return createDefaultContainerFactory(consumerReferenceFactory);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Command>> kafkaGroupReferenceListenerContainerFactory(
            ConsumerFactory<String, Command> consumerReferenceFactory) {
        return createDefaultContainerFactory(consumerReferenceFactory);
    }

    private KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Command>> createDefaultContainerFactory(
            ConsumerFactory<String, Command> consumerReferenceFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Command> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerReferenceFactory);
        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        factory.setConcurrency(1);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, FraudDataCandidate> consumerFraudCandidateFactory() {
        Map<String, Object> configs = consumerConfigs();
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, FraudDataCandidateDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, FraudDataCandidate>> kafkaFraudCandidateListenerContainerFactory(
            ConsumerFactory<String, FraudDataCandidate> consumerFraudCandidateFactory) {
        ConcurrentKafkaListenerContainerFactory<String, FraudDataCandidate> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFraudCandidateFactory);
        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        return factory;
    }

    @Bean
    public ProducerFactory<String, TBase> producerFactory() {
        Map<String, Object> configProps = kafkaProperties.buildProducerProperties();
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ThriftSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, TBase> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
