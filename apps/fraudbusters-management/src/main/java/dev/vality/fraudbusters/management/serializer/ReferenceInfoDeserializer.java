package dev.vality.fraudbusters.management.serializer;

import dev.vality.damsel.fraudbusters.ReferenceInfo;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReferenceInfoDeserializer extends AbstractThriftDeserializer<ReferenceInfo> {

    @Override
    public ReferenceInfo deserialize(String topic, byte[] data) {
        return deserialize(data, new ReferenceInfo());
    }
}
