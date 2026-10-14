package dev.vality.trusted.tokens.config;

import dev.vality.trusted.tokens.serde.PaymentSerde;
import dev.vality.trusted.tokens.serde.WithdrawalSerde;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaStreamsConfig {

    public static final String PAYMENT_SUFFIX = "-payment";
    public static final String WITHDRAWAL_SUFFIX = "-withdrawal";
    private static final String APP_ID = "trusted-tokens";

    @Value("${spring.kafka.client-id}")
    private String clientId;

    private final KafkaProperties kafkaProperties;

    @Bean
    public Properties paymentEventStreamProperties() {
        final Map<String, Object> props = kafkaProperties.buildStreamsProperties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID + PAYMENT_SUFFIX);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, clientId + PAYMENT_SUFFIX);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, PaymentSerde.class);
        addDefaultStreamsProperties(props);
        var properties = new Properties();
        properties.putAll(props);
        return properties;
    }

    @Bean
    public Properties withdrawalEventStreamProperties() {
        final Map<String, Object> props = kafkaProperties.buildStreamsProperties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID + WITHDRAWAL_SUFFIX);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, clientId + WITHDRAWAL_SUFFIX);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, WithdrawalSerde.class);
        addDefaultStreamsProperties(props);
        var properties = new Properties();
        properties.putAll(props);
        return properties;
    }

    private void addDefaultStreamsProperties(Map<String, Object> props) {
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        props.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);
        props.put(StreamsConfig.DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                LogAndFailExceptionHandler.class);
    }
}
