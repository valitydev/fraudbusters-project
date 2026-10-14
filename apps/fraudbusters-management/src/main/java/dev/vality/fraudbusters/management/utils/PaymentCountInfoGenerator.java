package dev.vality.fraudbusters.management.utils;

import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.converter.payment.PaymentCountInfoRequestToRowConverter;
import dev.vality.fraudbusters.management.converter.payment.PaymentListRecordToRowConverter;
import dev.vality.fraudbusters.management.exception.UnknownEventException;
import dev.vality.swag.fraudbusters.management.model.PaymentCountInfo;
import dev.vality.swag.fraudbusters.management.model.PaymentListRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PaymentCountInfoGenerator {

    private final CountInfoApiUtils countInfoApiUtils;
    private final PaymentListRecordToRowConverter paymentListRecordToRowConverter;
    private final PaymentCountInfoRequestToRowConverter countInfoListRecordToRowConverter;

    public PaymentCountInfo initDestination(String rowInfo, PaymentListRecord listRecord) {
        var paymentCountInfo = new PaymentCountInfo();
        paymentCountInfo.setListRecord(listRecord);
        if (StringUtils.hasLength(rowInfo)) {
            dev.vality.swag.fraudbusters.management.model.CountInfo countInfoValue =
                    countInfoApiUtils.initExternalRowCountInfo(rowInfo);
            paymentCountInfo.setCountInfo(countInfoValue);
        }
        return paymentCountInfo;
    }

    public Row initRow(PaymentCountInfo record, dev.vality.damsel.wb_list.ListType listType) {
        return switch (listType) {
            case black, white -> paymentListRecordToRowConverter.convert(record.getListRecord());
            case grey -> countInfoListRecordToRowConverter.convert(record);
            default -> throw new UnknownEventException();
        };
    }
}
