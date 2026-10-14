package dev.vality.trusted.tokens.factory;

import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.damsel.fraudbusters.WithdrawalStatus;
import dev.vality.trusted.tokens.constants.StreamType;
import dev.vality.trusted.tokens.exception.StreamInitializationException;
import dev.vality.trusted.tokens.serde.WithdrawalSerde;
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
public class WithdrawalEventByCardTokenFactory implements EventFactory {

    private final Properties withdrawalEventStreamProperties;
    private final Serde<Withdrawal> withdrawalSerde = new WithdrawalSerde();

    @Value("${kafka.topics.withdrawal.id}")
    private String source;
    @Value("${kafka.topics.withdrawal.dest}")
    private String destination;


    @Override
    public StreamType getType() {
        return StreamType.WITHDRAWAL;
    }

    @Override
    public KafkaStreams create() {
        try {
            StreamsBuilder builder = new StreamsBuilder();
            builder.stream(source, Consumed.with(Serdes.String(), withdrawalSerde))
                    .filter((key, value) -> isValidWithdrawal(value))
                    .selectKey((key, value) -> value.getDestinationResource().getBankCard().getToken())
                    .to(destination, Produced.with(Serdes.String(), withdrawalSerde));
            return new KafkaStreams(builder.build(), withdrawalEventStreamProperties);
        } catch (Exception e) {
            log.error("Error while create withdrawal stream: ", e);
            throw new StreamInitializationException(e);
        }
    }

    private boolean isValidWithdrawal(Withdrawal value) {
        return WithdrawalStatus.succeeded == value.getStatus()
                && value.getDestinationResource().isSetBankCard();
    }
}
