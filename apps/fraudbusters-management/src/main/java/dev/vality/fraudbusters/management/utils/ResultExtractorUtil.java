package dev.vality.fraudbusters.management.utils;

import dev.vality.fraudbusters.warehouse.Result;
import dev.vality.fraudbusters.warehouse.Row;
import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;

@UtilityClass
public class ResultExtractorUtil {

    public static Integer getIntegerField(Result result, String field) {
        if (CollectionUtils.isEmpty(result.getValues())) {
            return 0;
        }
        return result.getValues().stream()
                .findFirst()
                .map(Row::getValues)
                .map(rowFieldMap -> rowFieldMap.get(field))
                .map(Integer::parseInt)
                .orElse(0);
    }

    public static Float getFloatField(Result result, String field) {
        if (CollectionUtils.isEmpty(result.getValues())) {
            return 0.0f;
        }
        return result.getValues().stream()
                .findFirst()
                .map(Row::getValues)
                .map(rowFieldMap -> rowFieldMap.get(field))
                .map(Float::parseFloat)
                .orElse(0.0f);
    }
}
