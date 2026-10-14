package dev.vality.trusted.tokens.factory;

import dev.vality.trusted.tokens.constants.StreamType;
import org.apache.kafka.streams.KafkaStreams;

public interface EventFactory {

    StreamType getType();

    KafkaStreams create();
}
