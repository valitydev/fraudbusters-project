package dev.vality.fraudbusters.notificator.service;

import dev.vality.fraudbusters.warehouse.Query;
import dev.vality.fraudbusters.warehouse.Result;

public interface WarehouseQueryService {

    Result execute(Query query);

}
