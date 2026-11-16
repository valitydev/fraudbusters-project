package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.fraudbusters.management.domain.tables.pojos.PayoutListRecord;
import dev.vality.fraudbusters.management.utils.CountInfoApiUtils;
import dev.vality.swag.fraudbusters.management.model.PayoutListRow;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class PayoutListRecordToPayoutListRowConverter implements Converter<PayoutListRecord, PayoutListRow> {

    private final CountInfoApiUtils countInfoApiUtils;

    @Override
    public PayoutListRow convert(PayoutListRecord record) {
        var row = new PayoutListRow()
                .id(record.getId())
                .insertTime(record.getInsertTime())
                .createdByUser(record.getCreatedByUser())
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
