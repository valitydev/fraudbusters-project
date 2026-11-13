package dev.vality.fraudbusters.management.service.payout;

import dev.vality.damsel.wb_list.Command;
import dev.vality.damsel.wb_list.IdInfo;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.PayoutId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.converter.payout.PayoutListRecordToPayoutListRowConverter;
import dev.vality.fraudbusters.management.dao.payout.wblist.PayoutListDao;
import dev.vality.fraudbusters.management.domain.tables.pojos.PayoutListRecord;
import dev.vality.fraudbusters.management.exception.NotFoundException;
import dev.vality.fraudbusters.management.service.WbListCommandService;
import dev.vality.fraudbusters.management.utils.FilterRequestUtils;
import dev.vality.fraudbusters.management.utils.PagingDataUtils;
import dev.vality.fraudbusters.management.utils.PayoutListUtils;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import dev.vality.swag.fraudbusters.management.model.PayoutCountInfo;
import dev.vality.swag.fraudbusters.management.model.PayoutListRecordsResponse;
import dev.vality.swag.fraudbusters.management.model.PayoutRowListRequest;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PayoutListsService {

    private final PayoutListDao payoutListDao;
    private final WbListCommandService wbListCommandService;
    private final PayoutListRecordToPayoutListRowConverter payoutListRecordToPayoutListRowConverter;

    @Getter
    @Value("${parameters.payoutListNames}")
    private List<String> availableListNames;

    public PayoutListRecordsResponse filterLists(String listType, List<String> listNames, String searchValue,
                                                 String lastId, String sortOrder, Integer size) {
        var type = dev.vality.fraudbusters.management.domain.enums.ListType.valueOf(listType);
        String pattern = FilterRequestUtils.prepareSearchValue(searchValue);
        return new PayoutListRecordsResponse()
                .count(payoutListDao.countFilter(type, listNames, pattern))
                .result(payoutListDao.filter(type, listNames, pattern, lastId, PagingDataUtils.getSortOrder(sortOrder),
                                size)
                        .stream()
                        .map(payoutListRecordToPayoutListRowConverter::convert)
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

    /** The row is found by id, so its value never travels in the request. */
    public String removeRow(String id, String initiator) {
        PayoutListRecord record = payoutListDao.findById(id);
        if (record == null) {
            throw new NotFoundException("Payout list record not found: " + id);
        }
        ListType type = ListType.valueOf(record.getListType().getLiteral());
        Row row = createRow(type, record.getListName(), record.getValue(), record.getPartyId(),
                record.getWalletId());
        return wbListCommandService.sendCommandSync(row, type, Command.DELETE, initiator);
    }

    public List<String> getCurrentListNames(String listType) {
        return payoutListDao.getCurrentListNames(
                dev.vality.fraudbusters.management.domain.enums.ListType.valueOf(listType));
    }

    private Row toRow(ListType type, PayoutCountInfo record) {
        var listRecord = record.getListRecord();
        Row row = createRow(type, listRecord.getListName(), listRecord.getValue(), listRecord.getPartyId(),
                listRecord.getWalletId());
        if (type == ListType.grey) {
            row.setRowInfo(PayoutListUtils.toRowInfo(record.getCountInfo()));
        }
        return row;
    }

    private Row createRow(ListType type, String listName, String value, String partyId, String walletId) {
        if (!StringUtils.hasText(listName) || !StringUtils.hasText(value)) {
            throw new IllegalArgumentException("listName and value are required");
        }
        if (type != ListType.naming && !availableListNames.contains(listName)) {
            throw new IllegalArgumentException("Payouts have no list for " + listName);
        }
        PayoutScope.requirePartyForWallet(partyId, walletId);
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
}
