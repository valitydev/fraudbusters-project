package dev.vality.fraudbusters.notificator.service;

import dev.vality.fraudbusters.notificator.utils.TestObjectsFactory;
import dev.vality.fraudbusters.warehouse.Query;
import dev.vality.fraudbusters.warehouse.Result;
import dev.vality.fraudbusters.warehouse.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {QueryService.class, QueryParamsPreparationService.class})
class QueryServiceTest {

    @Autowired
    private QueryService queryService;

    @MockitoBean
    WarehouseQueryService warehouseQueryService;

    @MockitoBean
    TelegramBotsApi telegramBotsApi;

    @Test
    void queryWithEmptyResult() {
        String statement = "select * from table where id = :id";

        when(warehouseQueryService.execute(any(Query.class))).thenReturn(new Result());

        List<Map<String, String>> actualResult = queryService.query(statement);

        assertTrue(actualResult.isEmpty());

    }

    @Test
    void queryOk() {
        String statement = "select * from table where id = :id";
        Row firstRow = TestObjectsFactory.testRow();
        Row secondRow = TestObjectsFactory.testRow();
        Result result = new Result();
        List<Row> rows = List.of(firstRow, secondRow);
        result.setValues(rows);
        when(warehouseQueryService.execute(any(Query.class))).thenReturn(result);

        List<Map<String, String>> actualResult = queryService.query(statement);

        assertEquals(actualResult.size(), rows.size());
        assertTrue(actualResult.contains(firstRow.getValues()));
        assertTrue(actualResult.contains(secondRow.getValues()));
    }
}
