package dev.vality.trusted.tokens.factory;

import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.damsel.fraudbusters.PayoutStatus;
import dev.vality.trusted.tokens.constants.StreamType;
import dev.vality.trusted.tokens.exception.StreamInitializationException;
import dev.vality.trusted.tokens.serde.PayoutSerde;
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
public class PayoutEventByCardTokenFactory implements EventFactory {

    private final Properties payoutEventStreamProperties;
    private final Serde<Payout> payoutSerde = new PayoutSerde();

    @Value("${kafka.topics.payout.id}")
    private String source;
    @Value("${kafka.topics.payout.dest}")
    private String destination;


    @Override
    public StreamType getType() {
        return StreamType.PAYOUT;
    }

    @Override
    public KafkaStreams create() {
        try {
            StreamsBuilder builder = new StreamsBuilder();
            builder.stream(source, Consumed.with(Serdes.String(), payoutSerde))
                    .filter((key, value) -> isValidPayout(value))
                    .selectKey((key, value) -> value.getDestinationResource().getBankCard().getToken())
                    .to(destination, Produced.with(Serdes.String(), payoutSerde));
            return new KafkaStreams(builder.build(), payoutEventStreamProperties);
        } catch (Exception e) {
            log.error("Error while create payout stream: ", e);
            throw new StreamInitializationException(e);
        }
    }

    private boolean isValidPayout(Payout value) {
        return PayoutStatus.succeeded == value.getStatus()
                && value.getDestinationResource().isSetBankCard();
    }
}
