package dev.vality.wb.list.manager.handler;

import dev.vality.damsel.wb_list.*;
import dev.vality.wb.list.manager.exception.DbExecutionException;
import dev.vality.wb.list.manager.model.CountInfoModel;
import dev.vality.wb.list.manager.storage.ListStorage;
import dev.vality.wb.list.manager.storage.ListStorageRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class WbListServiceHandler implements WbListServiceSrv.Iface {

    private final ListStorageRegistry listStorageRegistry;
    private final JsonMapper objectMapper;

    @Override
    public boolean isExist(Row row) throws TException {
        try {
            return getCascadeRow(row).isPresent();
        } catch (DbExecutionException e) {
            log.error("WbListServiceHandler error when isExist row: {} e: ", row, e);
            throw new TException(e);
        }
    }

    @Override
    public boolean isAllExist(List<Row> list) throws TException {
        if (list != null && !list.isEmpty()) {
            for (Row row : list) {
                if (!isExist(row)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isAnyExist(List<Row> list) throws TException {
        if (list != null && !list.isEmpty()) {
            for (Row row : list) {
                if (isExist(row)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isNotOneExist(List<Row> list) throws TException {
        return !isAnyExist(list);
    }

    @Override
    public Result getRowInfo(Row row) {
        log.info("WbListServiceHandler getRowInfo row: {}", row);
        Optional<dev.vality.wb.list.manager.model.Row> result = getCascadeRow(row);
        if (result.isPresent() && row.getListType() == ListType.grey) {
            log.info("WbListServiceHandler getRowInfo result: {} isPresent=true!", result);
            try {
                CountInfoModel countInfoModel = objectMapper.readValue(result.get().getValue(), CountInfoModel.class);
                return new Result().setRowInfo(RowInfo.count_info(new CountInfo()
                        .setCount(countInfoModel.getCount())
                        .setTimeToLive(countInfoModel.getTtl())
                        .setStartCountTime(countInfoModel.getStartCountTime())
                ));
            } catch (JacksonException e) {
                log.error("Error when parse count info for row: {} e: ", row, e);
            }
        }
        log.info("WbListServiceHandler getRowInfo row: {} result: {} not present!", row, result);
        return new Result();
    }

    private Optional<dev.vality.wb.list.manager.model.Row> getCascadeRow(Row row) {
        ListStorage storage = listStorageRegistry.get(row);
        return storage.cascadeKeys(row).stream()
                .map(key -> storage.getRepository().get(key))
                .flatMap(Optional::stream)
                .findFirst();
    }

}
