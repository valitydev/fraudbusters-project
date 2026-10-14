package dev.vality.fraudbusters.notificator.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static dev.vality.fraudbusters.notificator.constant.CustomParameters.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryParamsPreparationServiceTest {

    QueryParamsPreparationService queryPrepareService = new QueryParamsPreparationService();

    @Test
    void prepare() {
        Map<String, String> params = queryPrepareService.prepare();

        assertEquals(4, params.size());
        assertTrue(params.containsKey(CURRENT_DATE_TIME));
        assertTrue(params.containsKey(CURRENT_DATE));
        assertTrue(params.containsKey(CURRENT_MONTH));
        assertTrue(params.containsKey(CURRENT_YEAR));
    }
}
