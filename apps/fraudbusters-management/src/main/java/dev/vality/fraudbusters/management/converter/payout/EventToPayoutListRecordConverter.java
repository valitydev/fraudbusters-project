package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.damsel.wb_list.Event;
import dev.vality.damsel.wb_list.PayoutId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.domain.tables.pojos.PayoutListRecord;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import dev.vality.fraudbusters.management.utils.RowUtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventToPayoutListRecordConverter implements Converter<Event, PayoutListRecord> {

    private final RowUtilsService rowUtilsService;

    @Override
    public PayoutListRecord convert(Event event) {
        Row row = event.getRow();
        PayoutId payoutId = row.getId().getPayoutId();
        PayoutScope.requirePartyForWallet(payoutId.getPartyId(), payoutId.getWalletId());
        PayoutListRecord record = new PayoutListRecord();
        record.setId(UUID.randomUUID().toString());
        record.setPartyId(PayoutScope.replaceNullWithEmpty(payoutId.getPartyId()));
        record.setWalletId(PayoutScope.replaceNullWithEmpty(payoutId.getWalletId()));
        record.setListType(rowUtilsService.initListType(row));
        record.setListName(row.getListName());
        record.setValue(row.getValue());
        record.setRowInfo(rowUtilsService.initRowInfo(row));
        record.setCreatedByUser(event.isSetUserInfo() ? event.getUserInfo().getUserId() : null);
        return record;
    }
}
