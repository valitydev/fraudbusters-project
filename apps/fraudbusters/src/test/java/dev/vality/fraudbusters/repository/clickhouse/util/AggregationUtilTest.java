package dev.vality.fraudbusters.repository.clickhouse.util;

import dev.vality.fraudbusters.fraud.model.FieldModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregationUtilTest {

    private static final long FROM = Instant.parse("2026-01-01T10:34:56.789Z").toEpochMilli();
    private static final long TO = Instant.parse("2026-01-02T11:05:00Z").toEpochMilli();
    private static final long FROM_SECOND = Instant.parse("2026-01-01T10:34:56Z").getEpochSecond();
    private static final long TO_SECOND = Instant.parse("2026-01-02T11:05:00Z").getEpochSecond();
    private static final long FROM_HOUR = Instant.parse("2026-01-01T10:00:00Z").toEpochMilli();
    private static final long TO_HOUR = Instant.parse("2026-01-02T11:00:00Z").toEpochMilli();
    private static final LocalDate FROM_DATE = LocalDate.parse("2026-01-01");
    private static final LocalDate TO_DATE = LocalDate.parse("2026-01-02");

    @Test
    void timeBoundsIncludeTheHoursOfTheWindow() {
        assertEquals(
                List.of(FROM_DATE, TO_DATE, FROM_SECOND, TO_SECOND, FROM_HOUR, TO_HOUR, "card",
                        "failed", "card_expired", "party"),
                AggregationUtil.generateParams(FROM, TO, List.of(new FieldModel("partyId", "party")), "card",
                        "failed", "card_expired"));
    }

    @Test
    void statusListParamsFollowTheHourBounds() {
        assertEquals(
                List.of(FROM_DATE, TO_DATE, FROM_SECOND, TO_SECOND, FROM_HOUR, TO_HOUR, "card",
                        "failed", "cancelled", "captured"),
                AggregationUtil.generateStatusesParams(FROM, TO, "card", AggregationUtil.getFinalStatuses()));
    }
}
