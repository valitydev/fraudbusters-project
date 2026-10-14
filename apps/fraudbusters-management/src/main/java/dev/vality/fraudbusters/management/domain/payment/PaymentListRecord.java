package dev.vality.fraudbusters.management.domain.payment;

import dev.vality.fraudbusters.management.domain.ListRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PaymentListRecord extends ListRecord {

    private String partyId;
    private String shopId;

}
