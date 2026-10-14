package dev.vality.fraudbusters.management.service.clickhouse;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsQueryTest {

    @Test
    void shouldCountUniqueAttemptedPayments() {
        assertThat(AnalyticsQuery.FRAUD_PAYMENTS_COUNT)
                .contains("uniqExact(id) AS count")
                .contains("shopId != 'TEST'")
                .doesNotContain("count(*) AS count");
    }

    @Test
    void shouldCalculateBlockedMetricsUsingUniquePayments() {
        assertThat(AnalyticsQuery.BLOCKED_FRAUD_PAYMENTS_COUNT)
                .contains("uniqExactIf(id,")
                .contains("shopId != 'TEST'");
        assertThat(AnalyticsQuery.BLOCKED_FRAUD_PAYMENTS_COUNT_RATIO)
                .contains("uniqExactIf(id,")
                .contains("/ uniqExact(id) AS ratio")
                .contains("shopId != 'TEST'");
    }
}
