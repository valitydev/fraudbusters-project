package dev.vality.fraudbusters.mg.connector.serde;


import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.fraudbusters.mg.connector.serde.deserializer.PaymentDeserializer;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class PaymentSerde implements Serde<Payment> {


    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<Payment> serializer() {
        return new ThriftSerializer<>();
    }

    @Override
    public Deserializer<Payment> deserializer() {
        return new PaymentDeserializer();
    }

}
