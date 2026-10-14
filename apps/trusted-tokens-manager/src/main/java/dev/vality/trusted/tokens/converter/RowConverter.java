package dev.vality.trusted.tokens.converter;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import dev.vality.trusted.tokens.model.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RowConverter {

    private final JsonMapper objectMapper;

    public Row convert(String key, Object value) {
        try {
            Row row = new Row();
            row.setKey(key);
            row.setValue(objectMapper.writeValueAsString(value));
            return row;
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

}
