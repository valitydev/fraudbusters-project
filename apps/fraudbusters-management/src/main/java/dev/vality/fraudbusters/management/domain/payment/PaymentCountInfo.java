package dev.vality.fraudbusters.management.domain.payment;

import dev.vality.fraudbusters.management.domain.CountInfo;
import lombok.Data;

@Data
public class PaymentCountInfo {

    private CountInfo countInfo;
    private PaymentListRecord listRecord;

}
