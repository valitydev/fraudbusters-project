package dev.vality.trusted.tokens.updater;

import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.model.CardTokensTransactionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class YearsDataUpdater {

    private final MonthsDataUpdater monthsDataUpdater;
    @Value("${trusted.tokens.keep-card-tokens-data-years}")
    private int keepDataYears;

    public Map<Integer, CardTokenData.YearsData> updateYearsData(
            Map<String, CardTokenData.CurrencyData> currencyMap, CardTokensTransactionInfo cardTokensTransactionInfo) {
        String currency = cardTokensTransactionInfo.getCurrency();
        int year = cardTokensTransactionInfo.getYear();
        Map<Integer, CardTokenData.YearsData> yearsMap = Optional.of(currencyMap)
                .map(map -> map.get(currency))
                .map(CardTokenData.CurrencyData::getYears)
                .orElse(new HashMap<>());
        yearsMap.keySet().removeIf(key -> key <= year - keepDataYears);
        yearsMap.put(year, CardTokenData.YearsData.builder()
                .yearSum(updateYearSum(yearsMap, cardTokensTransactionInfo))
                .yearCount(updateYearCount(yearsMap, cardTokensTransactionInfo))
                .months(monthsDataUpdater.updateMonthsData(yearsMap, cardTokensTransactionInfo))
                .build());
        return yearsMap;
    }

    private long updateYearSum(Map<Integer, CardTokenData.YearsData> yearsMap,
                               CardTokensTransactionInfo cardTokensTransactionInfo) {
        long amount = cardTokensTransactionInfo.getAmount();
        int year = cardTokensTransactionInfo.getYear();
        return Optional.of(yearsMap)
                .map(map -> map.get(year))
                .map(CardTokenData.YearsData::getYearSum)
                .map(sum -> sum + amount)
                .orElse(amount);
    }

    private int updateYearCount(Map<Integer, CardTokenData.YearsData> yearsMap,
                                CardTokensTransactionInfo cardTokensTransactionInfo) {
        int year = cardTokensTransactionInfo.getYear();
        return Optional.of(yearsMap)
                .map(map -> map.get(year))
                .map(CardTokenData.YearsData::getYearCount)
                .map(count -> count + 1)
                .orElse(1);
    }

}
