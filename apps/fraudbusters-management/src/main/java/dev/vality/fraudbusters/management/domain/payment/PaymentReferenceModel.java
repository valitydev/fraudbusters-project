package dev.vality.fraudbusters.management.domain.payment;

import dev.vality.fraudbusters.management.domain.ReferenceModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PaymentReferenceModel extends ReferenceModel {

    private String partyId;
    private String shopId;

}
