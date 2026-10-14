package dev.vality.fraudbusters.management.serializer;

import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FraudDataCandidateDeserializer extends AbstractThriftDeserializer<FraudDataCandidate> {

    @Override
    public FraudDataCandidate deserialize(String topic, byte[] data) {
        return deserialize(data, new FraudDataCandidate());
    }
}