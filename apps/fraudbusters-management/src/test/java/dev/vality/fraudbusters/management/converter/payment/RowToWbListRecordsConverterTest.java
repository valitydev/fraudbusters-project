package dev.vality.fraudbusters.management.converter.payment;

import tools.jackson.databind.json.JsonMapper;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.TestObjectFactory;
import dev.vality.fraudbusters.management.domain.enums.ListType;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListRecords;
import dev.vality.fraudbusters.management.utils.RowUtilsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RowToWbListRecordsConverterTest {

    private final JsonMapper objectMapper = JsonMapper.builder().build();
    private RowToWbListRecordsConverter converter;
    private RowUtilsService rowUtilsService;

    @BeforeEach
    void setUp() {
        rowUtilsService = new RowUtilsService(objectMapper);
        converter = new RowToWbListRecordsConverter(rowUtilsService);
    }

    @Test
    void shouldConvertSuccess() {
        Row row = TestObjectFactory.buildRow();

        WbListRecords wbListRecords = converter.convert(row);

        assertEquals(row.getValue(), wbListRecords.getValue());
        assertEquals(row.getListName(), wbListRecords.getListName());
        assertEquals(row.getId().getPaymentId().getPartyId(), wbListRecords.getPartyId());
        assertEquals(row.getId().getPaymentId().getShopId(), wbListRecords.getShopId());
        assertEquals(ListType.valueOf(row.getListType().name()), wbListRecords.getListType());
        String actualTimeToLive = wbListRecords.getTimeToLive().toInstant(
                ZoneOffset.UTC).toString();
        assertEquals(row.getRowInfo().getCountInfo().getTimeToLive(), actualTimeToLive);
        assertThat(wbListRecords.getRowInfo(), containsString(actualTimeToLive));
    }
}
