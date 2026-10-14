package dev.vality.trusted.tokens.factory;

import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.damsel.fraudbusters.PaymentStatus;
import dev.vality.trusted.tokens.constants.StreamType;
import dev.vality.trusted.tokens.exception.StreamInitializationException;
import dev.vality.trusted.tokens.serde.PaymentSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventByCardTokenFactory implements EventFactory {

    private final Properties paymentEventStreamProperties;
    private final Serde<Payment> paymentSerde = new PaymentSerde();

    @Value("${kafka.topics.payment.id}")
    private String source;
    @Value("${kafka.topics.payment.dest}")
    private String destination;

    @Override
    public StreamType getType() {
        return StreamType.PAYMENT;
    }

    @Override
    public KafkaStreams create() {
        try {
            StreamsBuilder builder = new StreamsBuilder();
            builder.stream(source, Consumed.with(Serdes.String(), paymentSerde))
                    .filter((key, value) -> isValidPayment(value))
                    .selectKey((key, value) -> value.getPaymentTool().getBankCard().getToken())
                    .to(destination, Produced.with(Serdes.String(), paymentSerde));
            return new KafkaStreams(builder.build(), paymentEventStreamProperties);
        } catch (Exception e) {
            log.error("Error while create payment stream: ", e);
            throw new StreamInitializationException(e);
        }
    }

    private boolean isValidPayment(Payment value) {
        return PaymentStatus.captured == value.getStatus()
                && value.getPaymentTool().isSetBankCard();
    }
}
