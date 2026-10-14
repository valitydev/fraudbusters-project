package dev.vality.trusted.tokens.serde;

import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import dev.vality.trusted.tokens.serde.deserializer.PaymentDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class PaymentSerde implements Serde<Payment> {

    @Override
    public Serializer<Payment> serializer() {
        return new ThriftSerializer<>();
    }

    @Override
    public Deserializer<Payment> deserializer() {
        return new PaymentDeserializer();
    }
}
