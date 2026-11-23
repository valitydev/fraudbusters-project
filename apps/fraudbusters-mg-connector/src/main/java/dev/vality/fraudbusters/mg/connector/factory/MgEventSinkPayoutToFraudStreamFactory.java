package dev.vality.fraudbusters.mg.connector.factory;

import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.fraudbusters.mg.connector.constant.StreamType;
import dev.vality.fraudbusters.mg.connector.exception.StreamInitializationException;
import dev.vality.fraudbusters.mg.connector.mapper.Mapper;
import dev.vality.fraudbusters.mg.connector.parser.EventParser;
import dev.vality.fraudbusters.mg.connector.serde.MachineEventSerde;
import dev.vality.fraudbusters.mg.connector.serde.PayoutSerde;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@ConditionalOnProperty(
        value = "fb.stream.payoutEnabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class MgEventSinkPayoutToFraudStreamFactory implements EventSinkFactory {

    private final Mapper<TimestampedChange, MachineEvent, Payout> logPayoutMapperDecorator;
    private final EventParser<TimestampedChange> payoutEventParser;
    private final RetryTemplate retryTemplate;
    private final Properties mgPayoutEventStreamProperties;
    private final Serde<MachineEvent> machineEventSerde = new MachineEventSerde();
    private final Serde<Payout> payoutSerde = new PayoutSerde();
    @Value("${kafka.topic.source.payout}")
    private String source;
    @Value("${kafka.topic.sink.payout}")
    private String sink;

    @Override
    public StreamType getType() {
        return StreamType.PAYOUT;
    }

    @Override
    public KafkaStreams create() {
        try {
            StreamsBuilder builder = new StreamsBuilder();

            builder.stream(source, Consumed.with(Serdes.String(), machineEventSerde))
                    .mapValues(machineEvent -> Map.entry(machineEvent, payoutEventParser.parseEvent(machineEvent)))
                    .filter((s, entry) -> filterChange(entry))
                    .mapValues(entry -> retryTemplate.execute(args ->
                                    logPayoutMapperDecorator.map(entry.getValue(), entry.getKey())
                            )
                    )
                    .filter((s, payout) -> payout != null)
                    .to(sink, Produced.with(Serdes.String(), payoutSerde));

            return new KafkaStreams(builder.build(), mgPayoutEventStreamProperties);
        } catch (Exception e) {
            log.error("Error when create stream e: ", e);
            throw new StreamInitializationException(e);
        }
    }

    private boolean filterChange(Map.Entry<MachineEvent, TimestampedChange> entry) {
        log.debug("filterChange entry: {}", entry);
        return entry.getValue().isSetChange() && logPayoutMapperDecorator.accept(entry.getValue());
    }

}
