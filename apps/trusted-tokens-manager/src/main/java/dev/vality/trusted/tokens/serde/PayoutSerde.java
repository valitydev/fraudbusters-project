package dev.vality.trusted.tokens.serde;

import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import dev.vality.trusted.tokens.serde.deserializer.PayoutDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class PayoutSerde implements Serde<Payout> {

    @Override
    public Serializer<Payout> serializer() {
        return new ThriftSerializer<>();
    }

    @Override
    public Deserializer<Payout> deserializer() {
        return new PayoutDeserializer();
    }
}
