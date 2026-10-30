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
            log.error("WbListServiceHandler error when isExist", e);
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
        log.debug("WbListServiceHandler getRowInfo");
        Optional<dev.vality.wb.list.manager.model.Row> result = getCascadeRow(row);
        if (result.isPresent() && row.getListType() == ListType.grey) {
            log.debug("WbListServiceHandler count info is present");
            try {
                CountInfoModel countInfoModel = objectMapper.readValue(result.get().getValue(), CountInfoModel.class);
                return new Result().setRowInfo(RowInfo.count_info(new CountInfo()
                        .setCount(countInfoModel.getCount())
                        .setTimeToLive(countInfoModel.getTtl())
                        .setStartCountTime(countInfoModel.getStartCountTime())
                ));
            } catch (JacksonException e) {
                log.error("Error when parsing count info", e);
            }
        }
        log.debug("WbListServiceHandler count info not present");
        return new Result();
    }

    private Optional<dev.vality.wb.list.manager.model.Row> getCascadeRow(Row row) {
        if (row.isSetId() && row.getId().isSetPayoutId()) {
            PayoutId id = row.getId().getPayoutId();
            // Validate the full scope before attempting a global fallback.
            KeyGenerator.generatePayoutKey(row.getListType(), row.getListName(), row.getValue(),
                    id.getPartyId(), id.getWalletId());
            return payoutRow(row, null, null)
                    .or(() -> payoutRow(row, id.getPartyId(), null))
                    .or(() -> payoutRow(row, id.getPartyId(), id.getWalletId()));
        }
        if (row.isSetId() && row.getId().isSetPaymentId()) {
            PaymentId paymentId = row.getId().getPaymentId();
            return cascadeGetRow(row.getListType(), row.getListName(), row.getValue(), paymentId.getPartyId(),
                    paymentId.getShopId());
        } else if (row.isSetId() && row.getId().isSetP2pId()) {
            throw new IllegalStateException("P2P is not supported. Row: " + row);
        }
        return cascadeGetRow(row.list_type, row.list_name, row.value, row.getPartyId(), row.getShopId());
    }

    private Optional<dev.vality.wb.list.manager.model.Row> payoutRow(Row row, String party, String wallet) {
        return listRepository.get(KeyGenerator.generatePayoutKey(row.getListType(), row.getListName(), row.getValue(),
                party, wallet), true);
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
