package dev.vality.fraudbusters.mg.connector.serde.deserializer;

import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentDeserializer extends AbstractThriftDeserializer<Payment> {

    @Override
    public Payment deserialize(String topic, byte[] data) {
        return deserialize(data, new Payment());
    }
}
