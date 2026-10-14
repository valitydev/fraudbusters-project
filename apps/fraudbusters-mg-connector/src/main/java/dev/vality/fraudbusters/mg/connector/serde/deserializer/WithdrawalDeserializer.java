package dev.vality.fraudbusters.mg.connector.serde.deserializer;

import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WithdrawalDeserializer extends AbstractThriftDeserializer<Withdrawal> {

    @Override
    public Withdrawal deserialize(String topic, byte[] data) {
        return deserialize(data, new Withdrawal());
    }
}
