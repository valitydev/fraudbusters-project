package dev.vality.trusted.tokens.utils;

import dev.vality.trusted.tokens.model.CardTokenData;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public abstract class CardTokenDataUtils {

    public static CardTokenData createCardTokenData() {
        CardTokenData cardTokenData = new CardTokenData();
        cardTokenData.setPayments(createCurrencyData(ConditionTemplateUtils.PAYMENT));
        cardTokenData.setWithdrawals(createCurrencyData(ConditionTemplateUtils.WITHDRAWAL));
        return cardTokenData;
    }

    private static Map<String, CardTokenData.CurrencyData> createCurrencyData(String transactionType) {
        int currentYear = LocalDateTime.now().getYear();
        CardTokenData.CurrencyData currencyData = new CardTokenData.CurrencyData();
        Map<Integer, CardTokenData.YearsData> yearsDataMap = new HashMap<>();
        for (int i = currentYear; i > currentYear - 4; i--) {
            CardTokenData.YearsData yearsData = new CardTokenData.YearsData();
            Map<Integer, CardTokenData.MonthsData> monthsDataMap = new HashMap<>();
            int yearCount = 0;
            long yearSum = 0;
            for (int j = 1; j <= 12; j++) {
                CardTokenData.MonthsData monthsData = new CardTokenData.MonthsData();
                monthsData.setMonthCount(j);
                if (transactionType.equals(ConditionTemplateUtils.PAYMENT)) {
                    monthsData.setMonthSum(j * 1000L);
                    yearSum += monthsData.getMonthSum();
                }
                monthsDataMap.put(j, monthsData);
                yearCount += monthsData.getMonthCount();
            }
            yearsData.setMonths(monthsDataMap);
            yearsData.setYearCount(yearCount);
            if (transactionType.equals(ConditionTemplateUtils.PAYMENT)) {
                yearsData.setYearSum(yearSum);
            }
            yearsDataMap.put(i, yearsData);
        }
        currencyData.setYears(yearsDataMap);
        Map<String, CardTokenData.CurrencyData> currencyDataMap = new HashMap<>();
        currencyDataMap.put("RUB", currencyData);
        currencyDataMap.put("EUR", currencyData);
        currencyDataMap.put("USD", currencyData);
        return currencyDataMap;
    }

    public static CardTokenData.YearsData getYearsData(long monthSum, int monthCount, int monthNumber) {
        CardTokenData.YearsData yearsData = new CardTokenData.YearsData();
        Map<Integer, CardTokenData.MonthsData> monthsDataMap = getMonthsDataMap(monthSum, monthCount, monthNumber);
        yearsData.setMonths(monthsDataMap);
        yearsData.setYearCount(monthCount * monthNumber);
        yearsData.setYearSum(monthSum * monthNumber);
        return yearsData;
    }

    private static Map<Integer, CardTokenData.MonthsData> getMonthsDataMap(long monthSum,
                                                                           int monthCount,
                                                                           int monthNumber) {
        Map<Integer, CardTokenData.MonthsData> monthsDataMap = new HashMap<>();
        for (int i = 1; i <= monthNumber; i++) {
            CardTokenData.MonthsData monthsData = getMonthsData(monthSum, monthCount);
            monthsDataMap.put(i, monthsData);
        }
        return monthsDataMap;
    }

    private static CardTokenData.MonthsData getMonthsData(long monthSum, int monthCount) {
        CardTokenData.MonthsData monthsData = new CardTokenData.MonthsData();
        monthsData.setMonthSum(monthSum);
        monthsData.setMonthCount(monthCount);
        return monthsData;
    }

}
