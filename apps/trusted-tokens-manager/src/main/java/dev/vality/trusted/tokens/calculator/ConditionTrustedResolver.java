package dev.vality.trusted.tokens.calculator;

import dev.vality.trusted.tokens.Condition;
import dev.vality.trusted.tokens.ConditionTemplate;
import dev.vality.trusted.tokens.model.CardTokenData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ConditionTrustedResolver {

    public boolean isTrusted(CardTokenData cardTokenData, ConditionTemplate conditionTemplate) {
        return cardTokenData != null
                && (isPaymentConditionTrusted(cardTokenData, conditionTemplate)
                || isWithdrawalConditionTrusted(cardTokenData, conditionTemplate));
    }

    private boolean isPaymentConditionTrusted(CardTokenData cardTokenData, ConditionTemplate conditionTemplate) {
        return conditionTemplate.getPaymentsConditions() != null
                && isConditionsTrusted(conditionTemplate.getPaymentsConditions().getConditions(),
                cardTokenData.getPayments());
    }

    private boolean isWithdrawalConditionTrusted(CardTokenData cardTokenData,
                                                 ConditionTemplate conditionTemplate) {
        return conditionTemplate.getWithdrawalsConditions() != null
                && isConditionsTrusted(conditionTemplate.getWithdrawalsConditions().getConditions(),
                cardTokenData.getWithdrawals());
    }

    private boolean isConditionsTrusted(List<Condition> conditions,
                                        Map<String, CardTokenData.CurrencyData> currencies) {
        return conditions.stream().anyMatch(condition -> isCurrencyDataTrusted(currencies, condition));
    }

    private boolean isCurrencyDataTrusted(Map<String, CardTokenData.CurrencyData> currencies,
                                          Condition condition) {
        Integer yearsOffset = condition.getYearsOffset().getValue();
        for (Map.Entry<String, CardTokenData.CurrencyData> currency : currencies.entrySet()) {
            if (currency.getKey().equals(condition.getCurrencySymbolicCode())) {
                long sum = YearsSumCalc.getSumYears(currency.getValue().getYears(), yearsOffset);
                int count = YearsCountCalc.getCountYears(currency.getValue().getYears(), yearsOffset);
                if ((sum > condition.getSum() || sum == 0) && count > condition.getCount()) {
                    return true;
                }
            }
        }
        return false;
    }
}
