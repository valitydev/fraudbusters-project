package dev.vality.fraudbusters.management.utils;

import dev.vality.fraudbusters.management.exception.DateFormatException;
import dev.vality.fraudbusters.management.utils.parser.CsvPaymentCountInfoParser;
import dev.vality.swag.fraudbusters.management.model.PaymentCountInfo;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvPaymentCountInfoParserTest {

    private static final String HEADER =
            "party_id,shop_id,list_name,value,count_info_count,count_info_start_time,count_info_end_time\n";

    private final CsvPaymentCountInfoParser parser = new CsvPaymentCountInfoParser();

    @Test
    void parsesGreyRowWithIsoTimes() {
        List<PaymentCountInfo> rows = parse(
                HEADER + "party-1,shop-1,EMAIL,grey@example.test,5,2026-11-16T10:00:00Z,2026-11-17T10:00:00Z\n");

        assertEquals(1, rows.size());
        assertEquals(5L, rows.get(0).getCountInfo().getCount());
        assertEquals(LocalDateTime.parse("2026-11-16T10:00:00"), rows.get(0).getCountInfo().getStartCountTime());
        assertEquals(LocalDateTime.parse("2026-11-17T10:00:00"), rows.get(0).getCountInfo().getEndCountTime());
    }

    @Test
    void rejectsGreyRowWithUnparsableTime() {
        assertThrows(DateFormatException.class, () -> parse(
                HEADER + "party-1,shop-1,EMAIL,grey@example.test,5,yesterday,2026-11-17T10:00:00Z\n"));
    }

    private List<PaymentCountInfo> parse(String csv) {
        return parser.parse(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
    }
}
