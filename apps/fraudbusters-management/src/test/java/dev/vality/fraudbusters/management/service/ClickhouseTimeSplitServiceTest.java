package dev.vality.fraudbusters.management.service;

import dev.vality.fraudbusters.management.domain.TimeSplitInfo;
import dev.vality.fraudbusters.management.service.iface.SqlTimeSplitService;
import dev.vality.swag.fraudbusters.management.model.SplitUnit;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class ClickhouseTimeSplitServiceTest {

    private final SqlTimeSplitService timeSplitService = new ClickhouseTimeSplitService();

    @Test
    void getSplitInfoWithDayUnit() {
        TimeSplitInfo splitInfo = timeSplitService.getSplitInfo(SplitUnit.DAY);

        assertThat(splitInfo.getTimeUnit(), containsString(SplitUnit.DAY.getValue()));
        assertThat(splitInfo.getStatement(), containsString(SplitUnit.DAY.getValue()));
    }

    @Test
    void getSplitInfoWithMinuteUnit() {
        TimeSplitInfo splitInfo = timeSplitService.getSplitInfo(SplitUnit.MINUTE);

        assertThat(splitInfo.getTimeUnit(), containsString(SplitUnit.MINUTE.getValue()));
        assertThat(splitInfo.getStatement(), containsString(SplitUnit.MINUTE.getValue()));
        assertThat(splitInfo.getTimeUnit(), containsString(SplitUnit.DAY.getValue()));
        assertThat(splitInfo.getStatement(), containsString(SplitUnit.DAY.getValue()));
        assertThat(splitInfo.getTimeUnit(), containsString(SplitUnit.HOUR.getValue()));
        assertThat(splitInfo.getStatement(), containsString(SplitUnit.HOUR.getValue()));
    }

    @Test
    void getSplitInfoWithMonthUnit() {
        TimeSplitInfo splitInfo = timeSplitService.getSplitInfo(SplitUnit.MONTH);

        assertThat(splitInfo.getTimeUnit(), containsString(SplitUnit.MONTH.getValue()));
        assertThat(splitInfo.getStatement(), containsString(SplitUnit.MONTH.getValue()));
        assertThat(splitInfo.getTimeUnit(), containsString(SplitUnit.YEAR.getValue()));
        assertThat(splitInfo.getStatement(), containsString(SplitUnit.YEAR.getValue()));
    }

    @Test
    void getSplitInfoWithHourUnit() {
        TimeSplitInfo splitInfo = timeSplitService.getSplitInfo(SplitUnit.HOUR);

        assertThat(splitInfo.getTimeUnit(), containsString(SplitUnit.HOUR.getValue()));
        assertThat(splitInfo.getStatement(), containsString(SplitUnit.HOUR.getValue()));
        assertThat(splitInfo.getTimeUnit(), containsString(SplitUnit.DAY.getValue()));
        assertThat(splitInfo.getStatement(), containsString(SplitUnit.DAY.getValue()));
    }

    @Test
    void getSplitInfoWithYearUnit() {
        TimeSplitInfo splitInfo = timeSplitService.getSplitInfo(SplitUnit.YEAR);

        assertThat(splitInfo.getTimeUnit(), containsString(SplitUnit.YEAR.getValue()));
        assertThat(splitInfo.getStatement(), containsString(SplitUnit.YEAR.getValue()));
    }

    @Test
    void getSplitInfoWithWeekUnit() {
        TimeSplitInfo splitInfo = timeSplitService.getSplitInfo(SplitUnit.WEEK);

        assertThat(splitInfo.getTimeUnit(), containsString(SplitUnit.WEEK.getValue()));
        assertThat(splitInfo.getStatement(), containsString(SplitUnit.WEEK.getValue()));
    }

}