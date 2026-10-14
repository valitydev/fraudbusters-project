package dev.vality.fraudbusters.management.domain.payment;

import dev.vality.fraudbusters.management.domain.GroupReferenceModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PaymentGroupReferenceModel extends GroupReferenceModel {

    private String partyId;
    private String shopId;

}
