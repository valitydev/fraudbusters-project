package dev.vality.fraudbusters.warehouse.service.impl;

import dev.vality.fraudbusters.warehouse.AbstractIntegrationTest;
import dev.vality.fraudbusters.warehouse.repository.QueryRepository;
import dev.vality.fraudbusters.warehouse.service.QueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class QueryServiceImplTest extends AbstractIntegrationTest {

    @Autowired
    private QueryService queryService;

    @MockitoBean
    private QueryRepository queryRepository;

    @Test
    void queryWithEmptyResult() {
        String select = "select";
        when(queryRepository.query(select, Collections.emptyMap())).thenReturn(Collections.emptyList());

        List<Map<String, String>> result = queryService.query(select, Collections.emptyMap());

        assertTrue(result.isEmpty());

    }

    @Test
    void queryWithResult() {
        String select = "select";
        Map<String, String> params = Map.of("param", "value");
        Map<String, Object> values = new HashMap<>();
        values.put("key", "value");
        List<Map<String, Object>> result = List.of(values);
        when(queryRepository.query(select, params)).thenReturn(result);

        List<Map<String, String>> actualResult = queryService.query(select, params);

        assertEquals(result, actualResult);

    }
}
