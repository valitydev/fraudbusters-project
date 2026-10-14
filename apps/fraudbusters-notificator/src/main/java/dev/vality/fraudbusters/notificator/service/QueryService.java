package dev.vality.fraudbusters.notificator.service;

import dev.vality.fraudbusters.warehouse.Query;
import dev.vality.fraudbusters.warehouse.Result;
import dev.vality.fraudbusters.warehouse.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class QueryService {

    private final QueryParamsPreparationService queryPrepareService;
    private final WarehouseQueryService warehouseQueryService;

    public List<Map<String, String>> query(String statement) {
        Query query = buildQuery(statement);
        Result result = warehouseQueryService.execute(query);
        if (!result.isSetValues()) {
            return Collections.emptyList();
        }
        return result.getValues().stream()
                .map(Row::getValues)
                .collect(Collectors.toList());
    }

    private Query buildQuery(String statement) {
        Map<String, String> params = queryPrepareService.prepare();
        Query query = new Query();
        query.setParams(params);
        query.setStatement(statement);
        return query;
    }

}
