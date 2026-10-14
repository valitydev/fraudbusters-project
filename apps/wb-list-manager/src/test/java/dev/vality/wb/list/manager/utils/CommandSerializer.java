package dev.vality.wb.list.manager.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.FilterProvider;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

import java.util.Map;

@Slf4j
public class CommandSerializer implements Serializer<ChangeCommandWrapper> {
    private final JsonMapper om = JsonMapper.builder().configureForJackson2().build();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, ChangeCommandWrapper data) {
        byte[] retVal = null;
        try {
            SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter
                    .serializeAllExcept("fieldMetaData");
            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter("myFilter", theFilter);
            retVal = om.writer(filters).writeValueAsString(data).getBytes();
        } catch (Exception e) {
            log.error("Error when serialize ThriftSerializer data: {} ", data, e);
        }
        return retVal;
    }

    @Override
    public void close() {

    }

}
