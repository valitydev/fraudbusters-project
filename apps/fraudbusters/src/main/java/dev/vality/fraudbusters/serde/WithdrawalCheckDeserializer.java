package dev.vality.fraudbusters.serde;

import dev.vality.fraudbusters.domain.PayoutResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
public class WithdrawalCheckDeserializer implements Deserializer<PayoutResult> {

    private final JsonMapper om = JsonMapper.builder().build();

    @Override
    public PayoutResult deserialize(String topic, byte[] data) {
        try {
            return om.readValue(data, PayoutResult.class);
        } catch (Exception e) {
            log.error("Error when deserialize PayoutResult topic: {}", topic, e);
            return null;
        }
    }

}
