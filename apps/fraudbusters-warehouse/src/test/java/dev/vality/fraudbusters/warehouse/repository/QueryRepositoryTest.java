package dev.vality.fraudbusters.warehouse.repository;

import dev.vality.fraudbusters.warehouse.AbstractIntegrationTest;
import dev.vality.fraudbusters.warehouse.query.TestQuery;
import dev.vality.testcontainers.annotations.clickhouse.ClickhouseTestcontainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ClickhouseTestcontainer(migrations = {
        "sql/db_init.sql",
        "sql/data/inserts_event_sink.sql"})
class QueryRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private QueryRepository queryRepository;

    @Test
    void queryWithoutParams() {
        List<Map<String, Object>> result =
                queryRepository.query(TestQuery.QUERY_METRIC_RECURRENT, Collections.emptyMap());

        Map<String, Object> stringObjectMap = result.get(0);
        assertEquals("ad8b7bfd-0760-4781-a400-51903ee8e504", String.valueOf(stringObjectMap.get("shopId")));
        assertEquals("166.66666666666666", String.valueOf(stringObjectMap.get("metric")));
    }

    @Test
    void queryWithParams() {
        Map<String, String> params = new HashMap<>();
        params.put("dateFrom", "2019-12-05");
        params.put("dateTo", "2019-12-12");
        List<Map<String, Object>> result = queryRepository.query(TestQuery.QUERY_METRIC_RECURRENT_WITH_PARAMS, params);

        Map<String, Object> stringObjectMap = result.get(0);
        assertEquals("ad8b7bfd-0760-4781-a400-51903ee8e504", String.valueOf(stringObjectMap.get("shopId")));
        assertEquals("166.66666666666666", String.valueOf(stringObjectMap.get("metric")));
    }
}
