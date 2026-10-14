package dev.vality.fraudbusters.mg.connector.factory;

import dev.vality.fraudbusters.mg.connector.constant.StreamType;
import org.apache.kafka.streams.KafkaStreams;

public interface EventSinkFactory {

    StreamType getType();

    KafkaStreams create();

}
