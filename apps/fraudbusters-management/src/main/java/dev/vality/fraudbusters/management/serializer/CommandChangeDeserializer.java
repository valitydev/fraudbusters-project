package dev.vality.fraudbusters.management.serializer;

import dev.vality.damsel.wb_list.ChangeCommand;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandChangeDeserializer extends AbstractThriftDeserializer<ChangeCommand> {

    @Override
    public ChangeCommand deserialize(String topic, byte[] data) {
        return deserialize(data, new ChangeCommand());
    }
}