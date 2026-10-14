package dev.vality.trusted.tokens.calculator;

import dev.vality.trusted.tokens.YearsOffset;
import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.utils.CardTokenDataUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YearsCountCalcTest {

    @Test
    void shouldEqualCountOfCurrentYearWithEmptyPreviousTwoYears() {
        Map<Integer, CardTokenData.YearsData> yearsDataMap = new HashMap<>();
        long monthSum = 200L;
        int monthCount = 2;
        int monthNumber = LocalDateTime.now().getMonthValue();
        CardTokenData.YearsData yearsDataForCurrentYear =
                CardTokenDataUtils.getYearsData(monthSum, monthCount, monthNumber);
        yearsDataMap.put(LocalDateTime.now().getYear(), yearsDataForCurrentYear);

        assertEquals(monthCount * monthNumber,
                YearsCountCalc.getCountYears(yearsDataMap, YearsOffset.current_with_two_last_years.getValue()));
    }


    @Test
    void shouldEqualCountOfCurrentYearWithEmptyPreviousYear() {
        Map<Integer, CardTokenData.YearsData> yearsDataMap = new HashMap<>();
        long monthSum = 200L;
        int monthCount = 2;
        int monthNumber = LocalDateTime.now().getMonthValue();
        CardTokenData.YearsData yearsDataForCurrentYear =
                CardTokenDataUtils.getYearsData(monthSum, monthCount, monthNumber);
        yearsDataMap.put(LocalDateTime.now().getYear(), yearsDataForCurrentYear);

        assertEquals(monthCount * monthNumber,
                YearsCountCalc.getCountYears(yearsDataMap, YearsOffset.current_with_last_years.getValue()));
    }

    @Test
    void shouldEqualCountOfCurrentYear() {
        Map<Integer, CardTokenData.YearsData> yearsDataMap = new HashMap<>();
        long monthSum = 200L;
        int monthCount = 2;
        int monthNumber = LocalDateTime.now().getMonthValue();
        CardTokenData.YearsData yearsDataForCurrentYear =
                CardTokenDataUtils.getYearsData(monthSum, monthCount, monthNumber);
        yearsDataMap.put(LocalDateTime.now().getYear(), yearsDataForCurrentYear);

        assertEquals(monthCount * monthNumber,
                YearsCountCalc.getCountYears(yearsDataMap, YearsOffset.current_year.getValue()));
    }

    @Test
    void shouldEqualCountOfTwoPreviousAndCurrentYears() {
        Map<Integer, CardTokenData.YearsData> yearsDataMap = new HashMap<>();
        long monthSum = 200L;
        int monthCount = 2;
        int monthNumber = LocalDateTime.now().getMonthValue();
        CardTokenData.YearsData yearsDataForCurrentYear =
                CardTokenDataUtils.getYearsData(monthSum, monthCount, monthNumber);
        CardTokenData.YearsData yearsDataForPreviousYear =
                CardTokenDataUtils.getYearsData(monthSum, monthCount, 12);
        CardTokenData.YearsData yearsDataForSecondPreviousYear =
                CardTokenDataUtils.getYearsData(monthSum, monthCount, 12);
        yearsDataMap.put(LocalDateTime.now().getYear(), yearsDataForCurrentYear);
        yearsDataMap.put(LocalDateTime.now().getYear() - 1, yearsDataForPreviousYear);
        yearsDataMap.put(LocalDateTime.now().getYear() - 2, yearsDataForSecondPreviousYear);

        long expectedCount = (monthCount * monthNumber) + (monthCount * 12) + (monthCount * (12 - monthNumber));
        assertEquals(expectedCount,
                YearsCountCalc.getCountYears(yearsDataMap, YearsOffset.current_with_two_last_years.getValue()));
    }

    @Test
    void shouldEqualCountOfPreviousAndCurrentYears() {
        Map<Integer, CardTokenData.YearsData> yearsDataMap = new HashMap<>();
        long monthSum = 200L;
        int monthCount = 2;
        int monthNumber = LocalDateTime.now().getMonthValue();
        CardTokenData.YearsData yearsDataForCurrentYear =
                CardTokenDataUtils.getYearsData(monthSum, monthCount, monthNumber);
        CardTokenData.YearsData yearsDataForPreviousYear =
                CardTokenDataUtils.getYearsData(monthSum, monthCount, 12);
        yearsDataMap.put(LocalDateTime.now().getYear(), yearsDataForCurrentYear);
        yearsDataMap.put(LocalDateTime.now().getYear(), yearsDataForPreviousYear);

        long expectedCount = (monthCount * monthNumber) + (monthCount * (12 - monthNumber));
        assertEquals(expectedCount,
                YearsCountCalc.getCountYears(yearsDataMap, YearsOffset.current_with_two_last_years.getValue()));
    }
}
