package dev.vality.fraudbusters.fraud.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayoutModel extends OperationModel {

    private String withdrawalId;
    private String walletId;

}
