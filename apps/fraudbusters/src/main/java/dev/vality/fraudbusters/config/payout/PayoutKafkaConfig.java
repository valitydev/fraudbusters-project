package dev.vality.fraudbusters.config.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.config.service.KafkaTemplateConfigurationService;
import dev.vality.fraudbusters.config.service.ListenersConfigurationService;
import dev.vality.fraudbusters.constant.GroupPostfix;
import dev.vality.fraudbusters.domain.PayoutResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@RequiredArgsConstructor
public class PayoutKafkaConfig {

    private final ListenersConfigurationService listenersConfigurationService;
    private final KafkaTemplateConfigurationService kafkaTemplateConfigurationService;

    @Bean
    public ConsumerFactory<String, Command> payoutTemplateListenerFactory() {
        return listenersConfigurationService.createDefaultConsumerFactory(GroupPostfix.PAYOUT_TEMPLATE_GROUP_ID);
    }

    @Bean
    public ConsumerFactory<String, Command> payoutGroupReferenceListenerFactory() {
        return listenersConfigurationService.createDefaultConsumerFactory(
                GroupPostfix.PAYOUT_GROUP_LIST_REFERENCE_GROUP_ID);
    }

    @Bean
    public ConsumerFactory<String, Command> payoutReferenceListenerFactory() {
        return listenersConfigurationService.createDefaultConsumerFactory(GroupPostfix.PAYOUT_REFERENCE_GROUP_ID);
    }

    @Bean
    public ConsumerFactory<String, Command> payoutGroupListenerFactory() {
        return listenersConfigurationService.createDefaultConsumerFactory(GroupPostfix.PAYOUT_GROUP_LIST_GROUP_ID);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Command> payoutGroupListenerContainerFactory(
            ConsumerFactory<String, Command> payoutGroupListenerFactory) {
        return listenersConfigurationService.createDefaultFactory(payoutGroupListenerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Command> payoutTemplateListenerContainerFactory(
            ConsumerFactory<String, Command> payoutTemplateListenerFactory) {
        return listenersConfigurationService.createDefaultFactory(payoutTemplateListenerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Command> payoutReferenceListenerContainerFactory(
            ConsumerFactory<String, Command> payoutReferenceListenerFactory) {
        return listenersConfigurationService.createDefaultFactory(payoutReferenceListenerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Command> payoutGroupReferenceListenerContainerFactory(
            ConsumerFactory<String, Command> payoutGroupReferenceListenerFactory) {
        return listenersConfigurationService.createDefaultFactory(payoutGroupReferenceListenerFactory);
    }

    @Bean
    public KafkaTemplate<String, PayoutResult> kafkaPayoutResultTemplate() {
        return new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(kafkaTemplateConfigurationService.producerJsonConfigs())
        );
    }
}
