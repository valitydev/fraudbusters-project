package dev.vality.fraudbusters.management.converter.payment;


import dev.vality.damsel.wb_list.IdInfo;
import dev.vality.damsel.wb_list.PaymentId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListRecords;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WbListRecordToRowConverter implements Converter<WbListRecords, Row> {

    @NonNull
    @Override
    public Row convert(WbListRecords source) {
        return new Row()
                .setListName(source.getListName())
                .setValue(source.getValue())
                .setId(IdInfo.payment_id(new PaymentId()
                        .setPartyId(source.getPartyId())
                        .setShopId(source.getShopId()))
                );
    }

}
