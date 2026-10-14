package dev.vality.wb.list.manager.serializer;


import dev.vality.damsel.wb_list.ChangeCommand;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandDeserializer extends AbstractThriftDeserializer<ChangeCommand> {

    @Override
    public ChangeCommand deserialize(String topic, byte[] data) {
        return super.deserialize(data, new ChangeCommand());
    }
}