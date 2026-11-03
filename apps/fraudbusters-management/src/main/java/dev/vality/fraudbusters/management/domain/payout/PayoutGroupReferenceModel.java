package dev.vality.fraudbusters.management.domain.payout;

import dev.vality.fraudbusters.management.domain.GroupReferenceModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PayoutGroupReferenceModel extends GroupReferenceModel {

    private String partyId;
    private String walletId;

}
