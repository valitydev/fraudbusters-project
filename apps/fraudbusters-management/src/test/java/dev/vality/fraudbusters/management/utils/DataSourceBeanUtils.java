package dev.vality.fraudbusters.management.utils;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.fraudbusters.*;
import dev.vality.swag.fraudbusters.management.model.ApplyRuleOnHistoricalDataSetRequest;
import dev.vality.swag.fraudbusters.management.model.PaymentReference;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.Set;

public class DataSourceBeanUtils {

    public static final String PARTY_ID = "partyId";
    public static final String SHOP_ID = "shopId";

    public static HistoricalDataSetCheckResult createHistoricalResponse() {
        return new HistoricalDataSetCheckResult()
                .setHistoricalTransactionCheck(Set.of(new HistoricalTransactionCheck()
                        .setCheckResult(new CheckResult()
                                .setConcreteCheckResult(new ConcreteCheckResult()
                                        .setResultStatus(ResultStatus.accept(new Accept()))
                                        .setRuleChecked("test"))
                                .setCheckedTemplate("test"))
                        .setTransaction(createDamselPayment())));
    }

    public static dev.vality.swag.fraudbusters.management.model.Payment createPayment() {
        return new dev.vality.swag.fraudbusters.management.model.Payment()
                .amount(100L)
                .currency("RUB")
                .id("1")
                .mobile(false)
                .recurrent(false)
                .status(dev.vality.swag.fraudbusters.management.model.Payment.StatusEnum.CAPTURED)
                .merchantInfo(new dev.vality.swag.fraudbusters.management.model.MerchantInfo()
                        .partyId(PARTY_ID)
                        .shopId(SHOP_ID));
    }

    public static Payment createDamselPayment() {
        return new Payment()
                .setId("1")
                .setPaymentTool(PaymentTool.bank_card(new BankCard()))
                .setCost(new Cash()
                        .setAmount(12L)
                        .setCurrency(new CurrencyRef()
                                .setSymbolicCode("RUB")))
                .setStatus(PaymentStatus.captured)
                .setPayerType(PayerType.payment_resource);
    }

    public static ApplyRuleOnHistoricalDataSetRequest createApplyRequst(
            dev.vality.swag.fraudbusters.management.model.Payment payment) {
        return new ApplyRuleOnHistoricalDataSetRequest()
                .dataSetId(1L)
                .reference(new PaymentReference()
                        .partyId(PARTY_ID)
                        .shopId(SHOP_ID))
                .template("test")
                .records(List.of(payment));
    }

    public static LinkedMultiValueMap<String, String> createParams() {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("paymentId", "test");
        params.add("size", "100");
        params.add("from", "2021-07-27 00:00:00");
        params.add("to", "2021-07-27 13:28:54");
        return params;
    }
}
