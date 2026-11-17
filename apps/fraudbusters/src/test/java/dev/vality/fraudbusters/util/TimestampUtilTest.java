package dev.vality.fraudbusters.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimestampUtilTest {

    @Test
    void getStringDate() {
        long epochSeconds = 1627988400L;
        assertEquals("2021-08-03T11:00:00Z", TimestampUtil.getStringDate(epochSeconds));
    }

    @ParameterizedTest
    @CsvSource({
            "2026-11-16 10:15:30, 2026-11-16T10:15:30",
            "2026-11-16T10:15:30Z, 2026-11-16T10:15:30",
            "2026-11-16T10:15:30.123Z, 2026-11-16T10:15:30.123",
            "2026-11-16T13:15:30+03:00, 2026-11-16T10:15:30",
            "2026-11-16T10:15:30, 2026-11-16T10:15:30"
    })
    void parseDateAcceptsManagementAndIsoFormats(String eventTime, String expectedUtc) {
        assertEquals(LocalDateTime.parse(expectedUtc), TimestampUtil.parseDate(eventTime));
    }
}
