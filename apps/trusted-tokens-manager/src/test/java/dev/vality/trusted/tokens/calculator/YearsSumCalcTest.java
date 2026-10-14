package dev.vality.trusted.tokens.calculator;

import dev.vality.trusted.tokens.YearsOffset;
import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.utils.CardTokenDataUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YearsSumCalcTest {

    @Test
    void shouldEqualSumOfCurrentYearWithEmptyPreviousTwoYears() {
        Map<Integer, CardTokenData.YearsData> yearsDataMap = new HashMap<>();
        long monthSum = 200L;
        int monthCount = 2;
        int monthNumber = LocalDateTime.now().getMonthValue();
        CardTokenData.YearsData yearsDataForCurrentYear =
                CardTokenDataUtils.getYearsData(monthSum, monthCount, monthNumber);
        yearsDataMap.put(LocalDateTime.now().getYear(), yearsDataForCurrentYear);

        assertEquals(monthSum * monthNumber,
                YearsSumCalc.getSumYears(yearsDataMap, YearsOffset.current_with_two_last_years.getValue()));
    }


    @Test
    void shouldEqualSumOfCurrentYearWithEmptyPreviousYear() {
        Map<Integer, CardTokenData.YearsData> yearsDataMap = new HashMap<>();
        long monthSum = 200L;
        int monthCount = 2;
        int monthNumber = LocalDateTime.now().getMonthValue();
        CardTokenData.YearsData yearsDataForCurrentYear =
                CardTokenDataUtils.getYearsData(monthSum, monthCount, monthNumber);
        yearsDataMap.put(LocalDateTime.now().getYear(), yearsDataForCurrentYear);

        assertEquals(monthSum * monthNumber,
                YearsSumCalc.getSumYears(yearsDataMap, YearsOffset.current_with_last_years.getValue()));
    }

    @Test
    void shouldEqualSumOfCurrentYear() {
        Map<Integer, CardTokenData.YearsData> yearsDataMap = new HashMap<>();
        long monthSum = 200L;
        int monthCount = 2;
        int monthNumber = LocalDateTime.now().getMonthValue();
        CardTokenData.YearsData yearsDataForCurrentYear =
                CardTokenDataUtils.getYearsData(monthSum, monthCount, monthNumber);
        yearsDataMap.put(LocalDateTime.now().getYear(), yearsDataForCurrentYear);

        assertEquals(monthSum * monthNumber,
                YearsSumCalc.getSumYears(yearsDataMap, YearsOffset.current_year.getValue()));
    }

    @Test
    void shouldEqualSumOfTwoPreviousAndCurrentYears() {
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

        long expectedSum = (monthSum * monthNumber) + (monthSum * 12) + (monthSum * (12 - monthNumber));
        assertEquals(expectedSum,
                YearsSumCalc.getSumYears(yearsDataMap, YearsOffset.current_with_two_last_years.getValue()));
    }

    @Test
    void shouldEqualSumOfPreviousAndCurrentYears() {
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

        long expectedSum = (monthSum * monthNumber) + (monthSum * (12 - monthNumber));
        assertEquals(expectedSum,
                YearsSumCalc.getSumYears(yearsDataMap, YearsOffset.current_with_two_last_years.getValue()));
    }
}
