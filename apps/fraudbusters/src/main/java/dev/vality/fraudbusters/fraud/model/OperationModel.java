package dev.vality.fraudbusters.fraud.model;

import dev.vality.fraudo.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Fields that payments and payouts share; history aggregates and rule resolvers work with this type. */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class OperationModel extends BaseModel {

    private String bin;
    private String lastDigits;
    private String binCountryCode;
    private String cardToken;
    private String partyId;

    private Long timestamp;

}
