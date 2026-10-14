package dev.vality.fraudbusters.mg.connector.serde;


import dev.vality.fraudbusters.mg.connector.serde.deserializer.MachineEventDeserializer;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class MachineEventSerde implements Serde<MachineEvent> {


    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<MachineEvent> serializer() {
        return new ThriftSerializer<>();
    }

    @Override
    public Deserializer<MachineEvent> deserializer() {
        return new MachineEventDeserializer();
    }

}
