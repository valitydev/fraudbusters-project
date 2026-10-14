package dev.vality.fraudbusters.management.domain.payment;

import dev.vality.fraudbusters.management.domain.DefaultReferenceModel;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DefaultPaymentReferenceModel extends DefaultReferenceModel {

    private String partyId;
    private String shopId;

}
