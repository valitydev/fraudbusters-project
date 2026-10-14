package dev.vality.trusted.tokens.calculator;

import dev.vality.trusted.tokens.model.CardTokenData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class YearsCountCalc {

    public static int getCountYears(Map<Integer, CardTokenData.YearsData> years, Integer yearsOffset) {
        if (yearsOffset == 0) {
            return years.get(LocalDateTime.now().getYear()).getYearCount();
        }
        Integer lastYearToCalc = LocalDateTime.now().getYear() - yearsOffset;
        CardTokenData.YearsData yearsData = years.get(lastYearToCalc);
        return calculateFullYearsCount(years, lastYearToCalc) + calculateMonthsCount(yearsData);
    }

    private static int calculateFullYearsCount(Map<Integer, CardTokenData.YearsData> years, Integer lastYearToCalc) {
        return years.entrySet().stream()
                .filter(year -> year.getKey() > lastYearToCalc)
                .mapToInt(year -> year.getValue().getYearCount())
                .sum();
    }

    private static int calculateMonthsCount(CardTokenData.YearsData lastYearData) {
        if (Objects.isNull(lastYearData)) {
            return 0;
        }
        Map<Integer, CardTokenData.MonthsData> monthsData = lastYearData.getMonths();
        Integer currentMonth = LocalDateTime.now().getMonthValue();
        return monthsData.entrySet().stream()
                .filter(month -> month.getKey() > currentMonth)
                .mapToInt(month -> month.getValue().getMonthCount())
                .sum();
    }
}
