package dev.vality.trusted.tokens.utils;

import dev.vality.trusted.tokens.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConditionTemplateUtils {

    public static final String PAYMENT = "payment";
    public static final String WITHDRAWAL = "withdrawal";

    public static ConditionTemplate createTemplateNotTrusted(String type) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(new Condition()
                .setCurrencySymbolicCode("RUB")
                .setYearsOffset(YearsOffset.current_year)
                .setCount(78)
                .setSum(PAYMENT.equals(type) ? 78000 : 0));
        return createTemplate(conditions, type);
    }

    public static ConditionTemplate createTemplateTrusted(String type) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(new Condition()
                .setCurrencySymbolicCode("RUB")
                .setYearsOffset(YearsOffset.current_with_two_last_years)
                .setCount(2)
                .setSum(PAYMENT.equals(type) ? 2000 : 0));
        return createTemplate(conditions, type);
    }

    public static ConditionTemplate createTemplateTrustedWithSeveralCurrency(String type) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(new Condition()
                .setCurrencySymbolicCode("RUB")
                .setYearsOffset(YearsOffset.current_with_two_last_years)
                .setCount(78)
                .setSum(PAYMENT.equals(type) ? 78000 : 0));
        conditions.add(new Condition()
                .setCurrencySymbolicCode("EUR")
                .setYearsOffset(YearsOffset.current_with_two_last_years)
                .setCount(2)
                .setSum(PAYMENT.equals(type) ? 1000 : 0));
        return createTemplate(conditions, type);
    }

    public static ConditionTemplate createTemplateNotTrustedWithSeveralCurrency(String type) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(new Condition()
                .setCurrencySymbolicCode("RUB")
                .setYearsOffset(YearsOffset.current_year)
                .setCount(79)
                .setSum(PAYMENT.equals(type) ? 78000 : 0));
        conditions.add(new Condition()
                .setCurrencySymbolicCode("EUR")
                .setYearsOffset(YearsOffset.current_year)
                .setCount(79)
                .setSum(PAYMENT.equals(type) ? 78000 : 0));
        conditions.add(new Condition()
                .setCurrencySymbolicCode("USD")
                .setYearsOffset(YearsOffset.current_year)
                .setCount(79)
                .setSum(PAYMENT.equals(type) ? 78000 : 0));
        return createTemplate(conditions, type);
    }

    public static ConditionTemplate createTemplateWithWithdrawalAndPayment() {
        return new ConditionTemplate()
                .setPaymentsConditions(new PaymentsConditions()
                        .setConditions(Collections.singletonList(new Condition()
                                .setCurrencySymbolicCode("RUB")
                                .setYearsOffset(YearsOffset.current_year)
                                .setCount(78)
                                .setSum(78000))))
                .setWithdrawalsConditions(new WithdrawalsConditions()
                        .setConditions(Collections.singletonList(new Condition()
                                .setCurrencySymbolicCode("RUB")
                                .setYearsOffset(YearsOffset.current_year)
                                .setCount(4))));
    }

    public static ConditionTemplate createTemplate(List<Condition> conditions,
                                                   String type) {
        ConditionTemplate conditionTemplate = new ConditionTemplate();
        if (PAYMENT.equals(type)) {
            PaymentsConditions paymentsConditions = new PaymentsConditions();
            paymentsConditions.setConditions(conditions);
            conditionTemplate.setPaymentsConditions(paymentsConditions);
        }

        if (WITHDRAWAL.equals(type)) {
            WithdrawalsConditions withdrawalsConditions = new WithdrawalsConditions();
            withdrawalsConditions.setConditions(conditions);
            conditionTemplate.setWithdrawalsConditions(withdrawalsConditions);
        }
        return conditionTemplate;
    }
}
