package dev.vality.wb.list.manager.handler;

import dev.vality.damsel.wb_list.*;
import dev.vality.wb.list.manager.exception.DbExecutionException;
import dev.vality.wb.list.manager.model.CountInfoModel;
import dev.vality.wb.list.manager.repository.ListRepository;
import dev.vality.wb.list.manager.utils.KeyGenerator;
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

    private final ListRepository listRepository;
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
        if (row.isSetId() && row.getId().isSetPaymentId()) {
            PaymentId paymentId = row.getId().getPaymentId();
            return cascadeGetRow(row.getListType(), row.getListName(), row.getValue(), paymentId.getPartyId(),
                    paymentId.getShopId());
        } else if (row.isSetId() && row.getId().isSetP2pId()) {
            throw new IllegalStateException("P2P is not supported. Row: " + row);
        }
        return cascadeGetRow(row.list_type, row.list_name, row.value, row.getPartyId(), row.getShopId());
    }

    private Optional<dev.vality.wb.list.manager.model.Row> cascadeGetRow(ListType listType,
                                                                         String listName,
                                                                         String value,
                                                                         String partyId,
                                                                         String shopId) {
        return Optional.ofNullable(
                listRepository.get(KeyGenerator.generateKey(listType, listName, value))
                        .orElse(listRepository.get(KeyGenerator.generateKey(listType, listName, value, partyId))
                                .orElse(listRepository
                                        .get(KeyGenerator.generateKey(listType, listName, value, partyId, shopId))
                                        .orElse(null))));
    }

}
