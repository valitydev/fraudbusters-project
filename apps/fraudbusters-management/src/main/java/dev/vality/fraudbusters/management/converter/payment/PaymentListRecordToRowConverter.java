package dev.vality.fraudbusters.management.converter.payment;


import dev.vality.damsel.wb_list.IdInfo;
import dev.vality.damsel.wb_list.PaymentId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.swag.fraudbusters.management.model.PaymentListRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentListRecordToRowConverter implements Converter<PaymentListRecord, Row> {

    @NonNull
    @Override
    public Row convert(PaymentListRecord source) {
        return new Row().setId(IdInfo.payment_id(new PaymentId()
                .setPartyId(source.getPartyId())
                .setShopId(source.getShopId())))
                .setListName(source.getListName())
                .setValue(source.getValue());
    }
}
