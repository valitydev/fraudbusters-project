package dev.vality.trusted.tokens.updater;

import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.model.CardTokensTransactionInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class MonthsDataUpdater {

    public Map<Integer, CardTokenData.MonthsData> updateMonthsData(
            Map<Integer, CardTokenData.YearsData> yearsMap, CardTokensTransactionInfo cardTokensTransactionInfo) {
        int year = cardTokensTransactionInfo.getYear();
        int month = cardTokensTransactionInfo.getMonth();
        Map<Integer, CardTokenData.MonthsData> monthMap = Optional.of(yearsMap)
                .map(map -> map.get(year))
                .map(CardTokenData.YearsData::getMonths)
                .orElse(new HashMap<>());
        monthMap.put(month, CardTokenData.MonthsData.builder()
                .monthSum(updateMonthSum(monthMap, cardTokensTransactionInfo))
                .monthCount(updateMonthCount(monthMap, cardTokensTransactionInfo))
                .build());
        return monthMap;
    }

    private long updateMonthSum(Map<Integer, CardTokenData.MonthsData> monthMap,
                                CardTokensTransactionInfo cardTokensTransactionInfo) {
        long amount = cardTokensTransactionInfo.getAmount();
        int month = cardTokensTransactionInfo.getMonth();
        return Optional.of(monthMap)
                .map(map -> map.get(month))
                .map(CardTokenData.MonthsData::getMonthSum)
                .map(sum -> sum + amount)
                .orElse(amount);
    }

    private int updateMonthCount(Map<Integer, CardTokenData.MonthsData> monthMap,
                                 CardTokensTransactionInfo cardTokensTransactionInfo) {
        int month = cardTokensTransactionInfo.getMonth();
        return Optional.of(monthMap)
                .map(map -> map.get(month))
                .map(CardTokenData.MonthsData::getMonthCount)
                .map(count -> count + 1)
                .orElse(1);
    }

}
