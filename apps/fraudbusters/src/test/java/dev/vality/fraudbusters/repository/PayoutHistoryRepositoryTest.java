package dev.vality.fraudbusters.repository;

import dev.vality.fraudbusters.config.payout.PayoutFraudoConfig;
import dev.vality.fraudbusters.exception.RuleFunctionException;
import dev.vality.fraudbusters.extension.ClickHouseContainerExtension;
import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.constant.PayoutCheckedField;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.fraud.payment.resolver.PaymentModelFieldResolver;
import dev.vality.fraudbusters.fraud.payout.aggregator.clickhouse.PayoutAggregators;
import dev.vality.fraudbusters.repository.clickhouse.impl.PayoutHistoryRepository;
import dev.vality.fraudbusters.service.TimeBoundaryServiceImpl;
import dev.vality.fraudbusters.util.BeanUtil;
import dev.vality.fraudo.model.TimeWindow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.clickhouse.ClickHouseDataSource;

import java.time.Instant;
import java.util.List;

import static dev.vality.fraudbusters.fraud.constant.PayoutCheckedField.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ClickHouseContainerExtension.class)
class PayoutHistoryRepositoryTest {

    private JdbcTemplate jdbc;
    private PayoutAggregators<PayoutModel, PayoutCheckedField> aggregates;
    private final TimeWindow window = TimeWindow.builder()
            .start(24)
            .timeUnit("hours")
            .build();
    private final long timestamp = Instant.parse("2026-01-01T12:00:00Z").toEpochMilli();

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(new ClickHouseDataSource("jdbc:clickhouse://localhost:" +
                ClickHouseContainerExtension.CLICKHOUSE_CONTAINER.getFirstMappedPort() + "/default"));
        jdbc.execute("TRUNCATE TABLE fraud.withdrawal");
        aggregates = new PayoutAggregators<>(new PayoutHistoryRepository(jdbc),
                new TimeBoundaryServiceImpl(), BeanUtil.createPayoutFieldsInPayoutHistory());
    }

    @Test
    void replaysEnrichmentAndStatusChangesAreCountedOnce() {
        // A legacy record is enriched without rewriting the old row.
        insert("old", 100, "succeeded", null, null, "RUB", 0);
        insert("old", 100, "succeeded", "party", "wallet", "RUB", 0);
        insert("a", 200, "succeeded", "party", "wallet", "RUB", 0);
        insert("b", 300, "failed", "party", "wallet", "RUB", 0);
        insert("b", 300, "failed", "party", "wallet", "RUB", 0);
        insert("c", 500, "failed", "party", "wallet", "USD", 0);
        insert("c", 600, "succeeded", "party", "wallet", "USD", 1000);
        insert("pending", 900, "pending", "party", "wallet", "RUB", 0);
        insert("other", 700, "succeeded", "other-party", "other-wallet", "RUB", 0);
        var model = createPayoutModel();
        var scope = List.of(PARTY_ID, WALLET_ID);
        assertEquals(4, aggregates.count(CARD_TOKEN, model, window, scope));
        assertEquals(5, aggregates.count(CARD_TOKEN, model, window, List.of()));
        assertEquals(3, aggregates.countSuccess(CARD_TOKEN, model, window, scope));
        assertEquals(1, aggregates.countError(CARD_TOKEN, model, window, scope));
        assertEquals(1, aggregates.countError(CARD_TOKEN, model, window, "synthetic-error", scope));
        assertEquals(0, aggregates.countError(CARD_TOKEN, model, window, "other-error", scope));
        assertEquals(1200.0, aggregates.sum(CARD_TOKEN, model, window, scope));
        assertEquals(900.0, aggregates.sumSuccess(CARD_TOKEN, model, window, scope));
        assertEquals(300.0, aggregates.sumError(CARD_TOKEN, model, window, scope));
        assertEquals(300.0, aggregates.sumError(CARD_TOKEN, model, window, "synthetic-error", scope));
        assertEquals(4, aggregates.countUniqueValue(CARD_TOKEN, model, BIN, window, scope));
        assertEquals(600.0, aggregates.sum(CARD_TOKEN, model, window, List.of(PARTY_ID, CURRENCY)));
    }

    @Test
    void missingValuesDoNotMatchLegacyRowsOrCountCurrentOperation() {
        insert("legacy", 100, "succeeded", null, null, "RUB", 0);
        var model = createPayoutModel();
        model.setCardToken(null);
        assertEquals(0, aggregates.count(CARD_TOKEN, model, window, List.of()));
        assertEquals(0.0, aggregates.sum(CARD_TOKEN, model, window, List.of()));
        model.setCardToken("synthetic-card");
        assertEquals(1, aggregates.count(CARD_TOKEN, model, window, List.of()));
        model.setWalletId(null);
        assertEquals(0, aggregates.count(CARD_TOKEN, model, window, List.of(WALLET_ID)));
    }

    @Test
    void rejectsFieldsThatPayoutHistoryDoesNotStore() {
        var model = createPayoutModel();
        assertThrows(RuleFunctionException.class, () -> aggregates.count(MASKED_PAN, model, window, List.of()));
        assertThrows(RuleFunctionException.class,
                () -> aggregates.count(CARD_TOKEN, model, window, List.of(MASKED_PAN)));
    }

    @Test
    void legacyAndEnrichedWithdrawalsCanBeWrittenThroughTheExistingBatchPath() {
        var withdrawal = new dev.vality.damsel.fraudbusters.Withdrawal()
                .setId("synthetic-batch")
                .setEventTime("2026-01-01T12:00:00Z")
                .setCost(new dev.vality.damsel.domain.Cash(100, new dev.vality.damsel.domain.CurrencyRef("RUB")))
                .setStatus(dev.vality.damsel.fraudbusters.WithdrawalStatus.succeeded)
                .setAccount(new dev.vality.damsel.fraudbusters.Account()
                        .setId("synthetic-account")
                        .setCurrency(new dev.vality.damsel.domain.CurrencyRef("RUB")))
                .setDestinationResource(dev.vality.damsel.fraudbusters.Resource.digital_wallet(
                        new dev.vality.damsel.fraudbusters.DigitalWallet()
                                .setId("synthetic-destination")));
        var writer = new dev.vality.fraudbusters.repository.clickhouse.impl.WithdrawalRepositoryImpl(jdbc,
                new org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate(jdbc),
                new dev.vality.fraudbusters.repository.clickhouse.mapper.WithdrawalMapper());
        writer.insertBatch(List.of(withdrawal));
        assertNull(jdbc.queryForObject("SELECT userId FROM fraud.withdrawal", String.class));
        withdrawal.setPartyId("party")
                .setWalletId("wallet")
                .setClientInfo(new dev.vality.damsel.fraudbusters.ClientInfo()
                        .setCustomerId("synthetic-customer")
                        .setUserId("synthetic-user")
                        .setEmail("synthetic@example.com"));
        writer.insertBatch(List.of(withdrawal));
        var model = createPayoutModel();
        model.setUserId("synthetic-user");
        model.setEmail("synthetic@example.com");
        assertEquals(1, aggregates.count(USER_ID, model, window, List.of(PARTY_ID, WALLET_ID)));
        assertEquals(1, aggregates.count(EMAIL, model, window, List.of(USER_ID)));
    }

    @Test
    void onlyOperationsInsideTheWindowAreCounted() {
        insert("inside", 100, "succeeded", "party", "wallet", "RUB", 0);
        insert("too-old", 200, "succeeded", "party", "wallet", "RUB", -25 * 3600 * 1000);
        insert("after-inspected", 300, "succeeded", "party", "wallet", "RUB", 60_000);
        var model = createPayoutModel();
        assertEquals(1, aggregates.count(CARD_TOKEN, model, window, List.of()));
        assertEquals(100.0, aggregates.sum(CARD_TOKEN, model, window, List.of()));
        var twoDays = TimeWindow.builder()
                .start(48)
                .timeUnit("hours")
                .build();
        assertEquals(2, aggregates.count(CARD_TOKEN, model, twoDays, List.of()));
        var olderHalf = TimeWindow.builder()
                .start(48)
                .end(24)
                .timeUnit("hours")
                .build();
        assertEquals(1, aggregates.count(CARD_TOKEN, model, olderHalf, List.of()));
    }

    @Test
    void paymentRulesReadPayoutHistoryWithPaymentValues() {
        insert("a", 100, "succeeded", "party", "wallet", "RUB", 0);
        insert("b", 200, "failed", "party", "other-wallet", "RUB", 0);
        insert("c", 400, "succeeded", "other-party", "wallet", "RUB", 0);
        var payment = new PaymentModel();
        payment.setCardToken("synthetic-card");
        payment.setPartyId("party");
        payment.setShopId("shop");
        payment.setCurrency("RUB");
        payment.setTimestamp(timestamp + 2000);
        var paymentAggregates = new PayoutAggregators<>(new PayoutHistoryRepository(jdbc),
                new TimeBoundaryServiceImpl(),
                new PayoutFraudoConfig().paymentFieldsInPayoutHistory(new PaymentModelFieldResolver()));
        assertEquals(3, paymentAggregates.count(PaymentCheckedField.CARD_TOKEN, payment, window, List.of()));
        assertEquals(2, paymentAggregates.count(PaymentCheckedField.CARD_TOKEN, payment, window,
                List.of(PaymentCheckedField.PARTY_ID)));
        assertEquals(1, paymentAggregates.countSuccess(PaymentCheckedField.CARD_TOKEN, payment, window,
                List.of(PaymentCheckedField.PARTY_ID, PaymentCheckedField.CURRENCY)));
        assertEquals(500.0, paymentAggregates.sumSuccess(PaymentCheckedField.CARD_TOKEN, payment, window,
                List.of()));
        assertThrows(RuleFunctionException.class, () -> paymentAggregates.count(PaymentCheckedField.CARD_TOKEN,
                payment, window, List.of(PaymentCheckedField.SHOP_ID)));
    }

    private PayoutModel createPayoutModel() {
        var model = new PayoutModel();
        model.setCardToken("synthetic-card");
        model.setPartyId("party");
        model.setWalletId("wallet");
        model.setCurrency("RUB");
        model.setAmount(999L);
        model.setTimestamp(timestamp + 2000);
        return model;
    }

    private void insert(String id, long amount, String status, String party, String wallet,
                        String currency, int offset) {
        jdbc.update("""
                INSERT INTO fraud.withdrawal(timestamp, eventTime, eventTimeHour, id, amount, currency,
                                             status, cardToken, bin, partyId, walletId, errorCode)
                VALUES (toDate(toDateTime(?, 'UTC')), ?, ?, ?, ?, ?, ?, 'synthetic-card', ?, ?, ?, 'synthetic-error')
                """, (timestamp + offset) / 1000, (timestamp + offset) / 1000, timestamp / 3600000 * 3600000, id,
                amount, currency, status, "bin-" + id, party, wallet);
    }
}
