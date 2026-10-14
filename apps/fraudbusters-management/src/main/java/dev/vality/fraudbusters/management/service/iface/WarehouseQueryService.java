package dev.vality.fraudbusters.management.service.iface;

import dev.vality.fraudbusters.warehouse.Query;
import dev.vality.fraudbusters.warehouse.Result;

public interface WarehouseQueryService {

    Result execute(Query query);

}
