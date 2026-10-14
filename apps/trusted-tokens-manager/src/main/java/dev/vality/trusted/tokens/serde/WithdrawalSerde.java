package dev.vality.trusted.tokens.serde;

import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import dev.vality.trusted.tokens.serde.deserializer.WithdrawalDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class WithdrawalSerde implements Serde<Withdrawal> {

    @Override
    public Serializer<Withdrawal> serializer() {
        return new ThriftSerializer<>();
    }

    @Override
    public Deserializer<Withdrawal> deserializer() {
        return new WithdrawalDeserializer();
    }
}
