package dev.vality.fraudbusters.warehouse.servlet.handler;

import dev.vality.fraudbusters.warehouse.AbstractIntegrationTest;
import dev.vality.fraudbusters.warehouse.Query;
import dev.vality.fraudbusters.warehouse.Result;
import dev.vality.fraudbusters.warehouse.service.QueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class QueryHandlerTest extends AbstractIntegrationTest {

    @Autowired
    private QueryHandler queryHandler;

    @MockitoBean
    private QueryService queryService;

    @Test
    void executeWithoutParams() {
        String statement = "select * from table";
        Query query = new Query();
        query.setStatement(statement);
        query.setParams(null);

        Map<String, String> values = new HashMap<>();
        values.put("key", "value");
        List<Map<String, String>> result = List.of(values);
        when(queryService.query(statement, Collections.emptyMap())).thenReturn(result);

        Result actualResult = queryHandler.execute(query);

        assertEquals(result.size(), actualResult.getValues().size());
        assertEquals(result.get(0), actualResult.getValues().get(0).getValues());
    }

    @Test
    void execute() {
        String statement = "select * from table where id = :id";
        Map<String, String> params = new HashMap<>();
        String value = "1";
        params.put("id", value);
        Query query = new Query();
        query.setStatement(statement);
        query.setParams(params);

        Map<String, String> values = new HashMap<>();
        values.put("key", "value");
        List<Map<String, String>> result = List.of(values);
        when(queryService.query(statement, params)).thenReturn(result);

        Result actualResult = queryHandler.execute(query);

        assertEquals(result.size(), actualResult.getValues().size());
        assertEquals(result.get(0), actualResult.getValues().get(0).getValues());
    }
}
