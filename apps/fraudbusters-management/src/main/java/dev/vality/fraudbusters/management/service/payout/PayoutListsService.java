package dev.vality.fraudbusters.management.service.payout;

import dev.vality.damsel.wb_list.Command;
import dev.vality.damsel.wb_list.IdInfo;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.PayoutId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.damsel.wb_list.RowInfo;
import dev.vality.fraudbusters.management.dao.payout.wblist.PayoutListDao;
import dev.vality.fraudbusters.management.domain.tables.pojos.PayoutListRecord;
import dev.vality.fraudbusters.management.service.WbListCommandService;
import dev.vality.fraudbusters.management.utils.CountInfoApiUtils;
import dev.vality.fraudbusters.management.utils.FilterRequestUtils;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import dev.vality.swag.fraudbusters.management.model.CountInfo;
import dev.vality.swag.fraudbusters.management.model.PayoutCountInfo;
import dev.vality.swag.fraudbusters.management.model.PayoutListRecordsResponse;
import dev.vality.swag.fraudbusters.management.model.PayoutListRow;
import dev.vality.swag.fraudbusters.management.model.PayoutRowListRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayoutListsService {

    private final PayoutListDao payoutListDao;
    private final WbListCommandService wbListCommandService;
    private final CountInfoApiUtils countInfoApiUtils;

    @Value("${parameters.payoutListNames}")
    private List<String> availableListNames;

    public PayoutListRecordsResponse filterLists(String listType, List<String> listNames, String searchValue,
                                                 Integer size) {
        var type = dev.vality.fraudbusters.management.domain.enums.ListType.valueOf(listType);
        String pattern = FilterRequestUtils.prepareSearchValue(searchValue);
        return new PayoutListRecordsResponse()
                .count(payoutListDao.countFilter(type, listNames, pattern))
                .result(payoutListDao.filter(type, listNames, pattern, size).stream()
                        .map(this::toApi)
                        .toList());
    }

    /** All records are checked before any is sent, so an invalid one does not leave the others half applied. */
    public List<String> insertRows(PayoutRowListRequest request, String initiator) {
        ListType type = ListType.valueOf(request.getListType().getValue());
        List<Row> rows = request.getRecords().stream()
                .map(record -> toRow(type, record))
                .toList();
        return rows.stream()
                .map(row -> wbListCommandService.sendCommandSync(row, type, Command.CREATE, initiator))
                .toList();
    }

    public String removeRow(String listType, String listName, String value, String partyId, String walletId,
                            String initiator) {
        ListType type = ListType.valueOf(listType);
        Row row = createRow(type, listName, value, partyId, walletId);
        return wbListCommandService.sendCommandSync(row, type, Command.DELETE, initiator);
    }

    public List<String> getCurrentListNames(String listType) {
        return payoutListDao.getCurrentListNames(
                dev.vality.fraudbusters.management.domain.enums.ListType.valueOf(listType));
    }

    public List<String> getAvailableListNames() {
        return availableListNames;
    }

    private Row toRow(ListType type, PayoutCountInfo record) {
        var listRecord = record.getListRecord();
        Row row = createRow(type, listRecord.getListName(), listRecord.getValue(), listRecord.getPartyId(),
                listRecord.getWalletId());
        if (type == ListType.grey) {
            row.setRowInfo(toRowInfo(record.getCountInfo()));
        }
        return row;
    }

    /** Without a start the grey list counts the whole history, as fraudbusters reads it. */
    private static RowInfo toRowInfo(CountInfo countInfo) {
        if (countInfo == null || countInfo.getCount() == null || countInfo.getEndCountTime() == null) {
            throw new IllegalArgumentException("A grey list record needs a count and an end of the period");
        }
        var info = new dev.vality.damsel.wb_list.CountInfo()
                .setCount(countInfo.getCount())
                .setTimeToLive(countInfo.getEndCountTime()
                        .toInstant(ZoneOffset.UTC)
                        .toString());
        if (countInfo.getStartCountTime() != null) {
            info.setStartCountTime(countInfo.getStartCountTime()
                    .toInstant(ZoneOffset.UTC)
                    .toString());
        }
        return RowInfo.count_info(info);
    }

    private Row createRow(ListType type, String listName, String value, String partyId, String walletId) {
        if (!StringUtils.hasText(listName) || !StringUtils.hasText(value)) {
            throw new IllegalArgumentException("listName and value are required");
        }
        if (type != ListType.naming && !availableListNames.contains(listName)) {
            throw new IllegalArgumentException("Payouts have no list for " + listName);
        }
        PayoutScope.validate(partyId, walletId);
        PayoutId payoutId = new PayoutId();
        if (StringUtils.hasText(partyId)) {
            payoutId.setPartyId(partyId);
            if (StringUtils.hasText(walletId)) {
                payoutId.setWalletId(walletId);
            }
        }
        return new Row()
                .setId(IdInfo.payout_id(payoutId))
                .setListName(listName)
                .setValue(value);
    }

    private PayoutListRow toApi(PayoutListRecord record) {
        var row = new PayoutListRow()
                .listType(dev.vality.swag.fraudbusters.management.model.ListType.fromValue(
                        record.getListType().getLiteral()))
                .listRecord(new dev.vality.swag.fraudbusters.management.model.PayoutListRecord()
                        .listName(record.getListName())
                        .value(record.getValue())
                        .partyId(StringUtils.hasText(record.getPartyId()) ? record.getPartyId() : null)
                        .walletId(StringUtils.hasText(record.getWalletId()) ? record.getWalletId() : null));
        if (StringUtils.hasText(record.getRowInfo())) {
            row.countInfo(countInfoApiUtils.initExternalRowCountInfo(record.getRowInfo()));
        }
        return row;
    }
}
