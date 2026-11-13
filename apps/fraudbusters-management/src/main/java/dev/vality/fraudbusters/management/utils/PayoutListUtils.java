package dev.vality.fraudbusters.management.utils;

import dev.vality.damsel.wb_list.RowInfo;
import dev.vality.swag.fraudbusters.management.model.CountInfo;
import lombok.experimental.UtilityClass;

import java.time.ZoneOffset;

@UtilityClass
public class PayoutListUtils {

    /** Without a start the grey list counts the whole history, as fraudbusters reads it. */
    public static RowInfo toRowInfo(CountInfo countInfo) {
        if (countInfo == null || countInfo.getCount() == null || countInfo.getEndCountTime() == null) {
            throw new IllegalArgumentException("A grey list record needs a count and an end of the period");
        }
        var info = new dev.vality.damsel.wb_list.CountInfo()
                .setCount(countInfo.getCount())
                .setTimeToLive(countInfo.getEndCountTime()
                        .toInstant(ZoneOffset.UTC)
                        .toString());
        if (countInfo.getStartCountTime() != null) {
            info.setStartCountTime(countInfo.getStartCountTime()
                    .toInstant(ZoneOffset.UTC)
                    .toString());
        }
        return RowInfo.count_info(info);
    }
}
