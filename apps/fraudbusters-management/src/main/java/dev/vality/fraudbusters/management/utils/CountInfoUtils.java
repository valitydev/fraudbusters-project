package dev.vality.fraudbusters.management.utils;

import tools.jackson.databind.json.JsonMapper;
import dev.vality.damsel.wb_list.Row;
import dev.vality.damsel.wb_list.RowInfo;
import dev.vality.fraudbusters.management.domain.CountInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tools.jackson.core.JacksonException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CountInfoUtils {

    private final JsonMapper objectMapper;

    public CountInfo initRowCountInfo(String rowInfo) {
        var countInfoValue = new CountInfo();
        try {
            var countInfo = objectMapper.readValue(rowInfo, dev.vality.damsel.wb_list.CountInfo.class);
            countInfoValue.setCount(countInfo.getCount());
            countInfoValue.setEndCountTime(countInfo.getTimeToLive());
            countInfoValue.setStartCountTime(countInfo.getStartCountTime());
        } catch (JacksonException e) {
            throw new RuntimeException("Error when read countInfo for rowInfo: " + rowInfo, e);
        }
        return countInfoValue;
    }

    public void initRowCountInfo(CountInfo countInfo, Row row) {
        String startCountTime = StringUtils.hasLength(countInfo.getStartCountTime())
                ? countInfo.getStartCountTime()
                : Instant.now().toString();
        row.setRowInfo(RowInfo.count_info(new dev.vality.damsel.wb_list.CountInfo()
                .setCount(countInfo.getCount())
                .setStartCountTime(startCountTime)
                .setTimeToLive(countInfo.getEndCountTime())));
    }

}
