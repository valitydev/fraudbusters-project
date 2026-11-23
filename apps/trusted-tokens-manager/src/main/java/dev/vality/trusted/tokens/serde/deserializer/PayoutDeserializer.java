package dev.vality.trusted.tokens.serde.deserializer;


import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PayoutDeserializer extends AbstractThriftDeserializer<Payout> {

    @SneakyThrows
    @Override
    public Payout deserialize(String topic, byte[] data) {
        try {
            return deserialize(data, new Payout());
        } catch (Exception e) {
            log.warn("Error when PayoutDeserializer deserialize e: ", e);
            throw e;
        }
    }

}
