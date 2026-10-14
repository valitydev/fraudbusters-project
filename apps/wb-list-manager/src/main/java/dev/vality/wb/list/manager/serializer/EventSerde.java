package dev.vality.wb.list.manager.serializer;


import dev.vality.damsel.wb_list.Event;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class EventSerde implements Serde<Event> {


    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<Event> serializer() {
        return new ThriftSerializer<>();
    }

    @Override
    public Deserializer<Event> deserializer() {
        return new EventDeserializer();
    }
}
