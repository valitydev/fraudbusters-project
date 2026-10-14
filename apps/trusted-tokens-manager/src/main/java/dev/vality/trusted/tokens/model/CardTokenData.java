package dev.vality.trusted.tokens.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
public class CardTokenData {

    private String lastPaymentId;
    private String lastWithdrawalId;
    private Map<String, CurrencyData> payments;
    private Map<String, CurrencyData> withdrawals;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CurrencyData {
        private Map<Integer, YearsData> years;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class YearsData {
        private long yearSum;
        private int yearCount;
        private Map<Integer, MonthsData> months;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthsData {
        private long monthSum;
        private int monthCount;
    }

}
