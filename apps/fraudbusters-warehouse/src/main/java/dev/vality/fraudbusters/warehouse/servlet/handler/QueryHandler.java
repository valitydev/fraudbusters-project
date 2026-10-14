package dev.vality.fraudbusters.warehouse.servlet.handler;


import dev.vality.fraudbusters.warehouse.Query;
import dev.vality.fraudbusters.warehouse.QueryServiceSrv;
import dev.vality.fraudbusters.warehouse.Result;
import dev.vality.fraudbusters.warehouse.convert.ResultConverter;
import dev.vality.fraudbusters.warehouse.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QueryHandler implements QueryServiceSrv.Iface {

    private final QueryService queryService;
    private final ResultConverter resultConverter;

    @Override
    public Result execute(Query query) {
        Map<String, String> params = query.isSetParams() ? query.getParams() : Collections.emptyMap();
        List<Map<String, String>> queryResult = queryService.query(query.getStatement(), params);
        return resultConverter.convert(queryResult);
    }
}
