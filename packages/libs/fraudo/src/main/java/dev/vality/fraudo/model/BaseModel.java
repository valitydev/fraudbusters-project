package dev.vality.fraudo.model;

import lombok.Data;

@Data
public class BaseModel {

    private String ip;
    private String email;
    private String phone;
    private String fingerprint;
    @lombok.ToString.Exclude
    private String customerId;
    @lombok.ToString.Exclude
    private String userId;
    private Long amount;
    private String currency;
    private String paymentSystem;
    private String cardCategory;

}
