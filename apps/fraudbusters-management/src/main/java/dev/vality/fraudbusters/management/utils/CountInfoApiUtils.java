package dev.vality.fraudbusters.management.utils;

import tools.jackson.databind.json.JsonMapper;
import dev.vality.damsel.wb_list.RowInfo;
import dev.vality.swag.fraudbusters.management.model.CountInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tools.jackson.core.JacksonException;
import java.time.Instant;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class CountInfoApiUtils {

    private final JsonMapper objectMapper;

    public CountInfo initExternalRowCountInfo(String rowInfo) {
        var countInfoValue = new CountInfo();
        try {
            var countInfo = objectMapper.readValue(rowInfo, dev.vality.damsel.wb_list.CountInfo.class);
            countInfoValue.setCount(countInfo.getCount());
            countInfoValue.setEndCountTime(DateTimeUtils.toDate(countInfo.getTimeToLive()));
            countInfoValue.setStartCountTime(DateTimeUtils.toDate(countInfo.getStartCountTime()));
        } catch (JacksonException e) {
            throw new RuntimeException("Error when read countInfo for rowInfo: " + rowInfo, e);
        }
        return countInfoValue;
    }

    public RowInfo initRowInfo(CountInfo countInfo) {
        String startCountTime =
                StringUtils.hasLength(countInfo.getStartCountTime().toInstant(ZoneOffset.UTC).toString())
                        ? countInfo.getStartCountTime().toInstant(ZoneOffset.UTC).toString()
                        : Instant.now().toString();
        return RowInfo.count_info(new dev.vality.damsel.wb_list.CountInfo()
                .setCount(countInfo.getCount())
                .setStartCountTime(startCountTime)
                .setTimeToLive(countInfo.getEndCountTime().toInstant(ZoneOffset.UTC).toString()));
    }
}
