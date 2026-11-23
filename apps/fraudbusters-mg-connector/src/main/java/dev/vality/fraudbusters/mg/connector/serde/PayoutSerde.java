package dev.vality.fraudbusters.mg.connector.serde;


import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.fraudbusters.mg.connector.serde.deserializer.PayoutDeserializer;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class PayoutSerde implements Serde<Payout> {


    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<Payout> serializer() {
        return new ThriftSerializer<>();
    }

    @Override
    public Deserializer<Payout> deserializer() {
        return new PayoutDeserializer();
    }

}
