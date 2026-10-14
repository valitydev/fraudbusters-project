package dev.vality.fraudbusters.management.converter.payment;


import dev.vality.damsel.wb_list.PaymentId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListRecords;
import dev.vality.fraudbusters.management.utils.RowUtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RowToWbListRecordsConverter implements Converter<Row, WbListRecords> {

    private final RowUtilsService rowUtilsService;

    @Override
    public WbListRecords convert(Row destination) {
        WbListRecords wbListRecords = new WbListRecords();

        if (destination.isSetId() && destination.getId().isSetPaymentId()) {
            PaymentId paymentId = destination.getId().getPaymentId();
            wbListRecords.setPartyId(paymentId.getPartyId());
            wbListRecords.setShopId(paymentId.getShopId());
        } else {
            wbListRecords.setPartyId(wbListRecords.getPartyId());
            wbListRecords.setShopId(wbListRecords.getShopId());
        }

        wbListRecords.setListName(destination.getListName());
        wbListRecords.setListType(rowUtilsService.initListType(destination));
        wbListRecords.setValue(destination.getValue());
        wbListRecords.setTimeToLive(rowUtilsService.getTimeToLive(destination));
        wbListRecords.setRowInfo(rowUtilsService.initRowInfo(destination));
        return wbListRecords;
    }

}
