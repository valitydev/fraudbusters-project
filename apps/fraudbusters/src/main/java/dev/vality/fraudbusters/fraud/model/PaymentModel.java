package dev.vality.fraudbusters.fraud.model;

import dev.vality.fraudo.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.vality.damsel.fraudbusters.OperationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PaymentModel extends BaseModel {

    @JsonIgnore
    private OperationType operationType = OperationType.payment;

    private String bin;
    private String lastDigits;
    private String binCountryCode;
    private String cardToken;
    private String shopId;
    private String partyId;
    @JsonIgnore
    private String walletId;

    private Long timestamp;

    private boolean mobile;
    private boolean recurrent;

}
