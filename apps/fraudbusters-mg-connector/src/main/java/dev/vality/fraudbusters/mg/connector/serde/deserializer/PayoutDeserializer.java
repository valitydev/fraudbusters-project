package dev.vality.fraudbusters.mg.connector.serde.deserializer;

import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PayoutDeserializer extends AbstractThriftDeserializer<Payout> {

    @Override
    public Payout deserialize(String topic, byte[] data) {
        return deserialize(data, new Payout());
    }
}
