package dev.vality.fraudbusters.fraud.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PaymentModel extends OperationModel {

    private String shopId;

    private boolean mobile;
    private boolean recurrent;

}
