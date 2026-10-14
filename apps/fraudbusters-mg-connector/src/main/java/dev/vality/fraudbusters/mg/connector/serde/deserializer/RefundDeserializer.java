package dev.vality.fraudbusters.mg.connector.serde.deserializer;

import dev.vality.damsel.fraudbusters.Refund;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RefundDeserializer extends AbstractThriftDeserializer<Refund> {

    @Override
    public Refund deserialize(String topic, byte[] data) {
        return deserialize(data, new Refund());
    }
}
