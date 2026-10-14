package dev.vality.fraudbusters.management.service;

import dev.vality.fraudbusters.management.exception.WarehouseQueryException;
import dev.vality.fraudbusters.management.service.iface.WarehouseQueryService;
import dev.vality.fraudbusters.warehouse.Query;
import dev.vality.fraudbusters.warehouse.QueryServiceSrv;
import dev.vality.fraudbusters.warehouse.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseQueryServiceImpl implements WarehouseQueryService {

    private final QueryServiceSrv.Iface service;

    @Override
    public Result execute(Query query) {
        try {
            return service.execute(query);
        } catch (TException e) {
            log.error("Error call warehouse query service", e);
            throw new WarehouseQueryException(e);
        }
    }
}
