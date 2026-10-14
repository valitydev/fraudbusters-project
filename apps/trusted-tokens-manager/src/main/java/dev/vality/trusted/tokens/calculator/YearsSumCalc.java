package dev.vality.trusted.tokens.calculator;

import dev.vality.trusted.tokens.model.CardTokenData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class YearsSumCalc {

    public static long getSumYears(Map<Integer, CardTokenData.YearsData> years, Integer yearsOffset) {
        if (yearsOffset == 0) {
            return years.get(LocalDateTime.now().getYear()).getYearSum();
        }
        Integer lastYearToCalc = LocalDateTime.now().getYear() - yearsOffset;
        CardTokenData.YearsData yearsData = years.get(lastYearToCalc);
        return calculateFullYearsSum(years, lastYearToCalc) + calculateMonthsSum(yearsData);
    }

    private static long calculateFullYearsSum(Map<Integer, CardTokenData.YearsData> years, Integer lastYearToCalc) {
        return years.entrySet().stream()
                .filter(year -> year.getKey() > lastYearToCalc)
                .mapToLong(year -> year.getValue().getYearSum())
                .sum();
    }

    private static long calculateMonthsSum(CardTokenData.YearsData lastYearData) {
        if (Objects.isNull(lastYearData)) {
            return 0;
        }
        Map<Integer, CardTokenData.MonthsData> monthsData = lastYearData.getMonths();
        Integer currentMonth = LocalDateTime.now().getMonthValue();
        return monthsData.entrySet().stream()
                .filter(month -> month.getKey() > currentMonth)
                .mapToLong(month -> month.getValue().getMonthSum())
                .sum();
    }
}
