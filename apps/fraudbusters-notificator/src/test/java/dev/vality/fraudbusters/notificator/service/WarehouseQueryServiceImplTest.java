package dev.vality.fraudbusters.notificator.service;

import dev.vality.fraudbusters.notificator.utils.TestObjectsFactory;
import dev.vality.fraudbusters.notificator.exception.WarehouseQueryException;
import dev.vality.fraudbusters.warehouse.Query;
import dev.vality.fraudbusters.warehouse.QueryServiceSrv;
import dev.vality.fraudbusters.warehouse.Result;
import dev.vality.fraudbusters.warehouse.Row;
import lombok.SneakyThrows;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WarehouseQueryServiceImpl.class})
class WarehouseQueryServiceImplTest {

    @Autowired
    private WarehouseQueryService warehouseQueryService;

    @MockitoBean
    QueryServiceSrv.Iface queryService;

    @SneakyThrows
    @Test
    void executeErrorCall() throws TException {

        when(queryService.execute(any(Query.class))).thenThrow(new TException("Error"));

        assertThrows(WarehouseQueryException.class, () -> warehouseQueryService.execute(new Query()));

    }

    @SneakyThrows
    @Test
    void executeOk() throws TException {
        Query query = new Query();
        query.setStatement(TestObjectsFactory.randomString());
        query.setParams(Map.of(TestObjectsFactory.randomString(), TestObjectsFactory.randomString()));
        Row row = new Row();
        row.setValues(Map.of(TestObjectsFactory.randomString(), TestObjectsFactory.randomString()));
        Result result = new Result();
        result.setValues(List.of(row));
        when(queryService.execute(query)).thenReturn(result);

        Result actualResult = warehouseQueryService.execute(query);

        assertEquals(result, actualResult);

    }
}