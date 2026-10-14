package dev.vality.fraudbusters.warehouse.convert;

import dev.vality.fraudbusters.warehouse.Result;
import dev.vality.fraudbusters.warehouse.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ResultConverter implements Converter<List<Map<String, String>>, Result> {

    @Override
    public Result convert(List<Map<String, String>> queryResult) {
        return new Result()
                .setValues(queryResult.stream()
                        .map(rowValues -> new Row()
                                .setValues(rowValues))
                        .collect(Collectors.toList())
                );
    }
}
