package dev.vality.fraudbusters.fraud.payout;

import dev.vality.fraudbusters.extension.ClickHouseContainerExtension;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.repository.clickhouse.impl.PayoutHistoryRepository;
import dev.vality.fraudbusters.service.TimeBoundaryServiceImpl;
import dev.vality.fraudo.model.TimeWindow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.yandex.clickhouse.ClickHouseDataSource;

import java.time.Instant;
import java.util.List;

import static dev.vality.fraudbusters.fraud.constant.CheckedField.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ClickHouseContainerExtension.class)
class PayoutHistoryRepositoryTest {

    private JdbcTemplate jdbc;
    private PayoutAggregators<PayoutModel> aggregates;
    private final TimeWindow window = TimeWindow.builder().start(24).timeUnit("hours").build();
    private final long timestamp = Instant.parse("2026-01-01T12:00:00Z").toEpochMilli();

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(new ClickHouseDataSource("jdbc:clickhouse://localhost:" +
                ClickHouseContainerExtension.CLICKHOUSE_CONTAINER.getFirstMappedPort() + "/default"));
        jdbc.execute("TRUNCATE TABLE fraud.withdrawal");
        aggregates = new PayoutAggregators<>(new PayoutHistoryRepository(new NamedParameterJdbcTemplate(jdbc)),
                new TimeBoundaryServiceImpl());
    }

    @Test
    void replaysEnrichmentAndStatusChangesAreCountedOnce() {
        // A legacy record is enriched without rewriting the old row.
        insert("old", 100, "succeeded", null, null, null, "RUB", 0);
        insert("old", 100, "succeeded", "synthetic-user", "party", "wallet", "RUB", 0);
        insert("a", 200, "succeeded", "synthetic-user", "party", "wallet", "RUB", 0);
        insert("b", 300, "failed", "synthetic-user", "party", "wallet", "RUB", 0);
        insert("b", 300, "failed", "synthetic-user", "party", "wallet", "RUB", 0);
        insert("c", 500, "failed", "synthetic-user", "party", "wallet", "USD", 0);
        insert("c", 600, "succeeded", "synthetic-user", "party", "wallet", "USD", 1000);
        insert("pending", 900, "pending", "synthetic-user", "party", "wallet", "RUB", 0);
        insert("other", 700, "succeeded", "synthetic-user", "other-party", "other-wallet", "RUB", 0);
        var model = model();
        var scope = List.of(PARTY_ID, WALLET_ID);
        assertEquals(4, aggregates.count(USER_ID, model, window, scope));
        assertEquals(5, aggregates.count(USER_ID, model, window, List.of()));
        assertEquals(3, aggregates.countSuccess(USER_ID, model, window, scope));
        assertEquals(1, aggregates.countError(USER_ID, model, window, scope));
        assertEquals(1, aggregates.countError(USER_ID, model, window, "synthetic-error", scope));
        assertEquals(0, aggregates.countError(USER_ID, model, window, "other-error", scope));
        assertEquals(1200.0, aggregates.sum(USER_ID, model, window, scope));
        assertEquals(900.0, aggregates.sumSuccess(USER_ID, model, window, scope));
        assertEquals(300.0, aggregates.sumError(USER_ID, model, window, scope));
        assertEquals(300.0, aggregates.sumError(USER_ID, model, window, "synthetic-error", scope));
        assertEquals(4, aggregates.countUniqueValue(USER_ID, model, CARD_TOKEN, window, scope));
        assertEquals(600.0, aggregates.sum(USER_ID, model, window, List.of(PARTY_ID, CURRENCY)));
    }

    @Test
    void missingIdentityDoesNotMatchLegacyRowsOrCountCurrentOperation() {
        insert("legacy", 100, "succeeded", null, null, null, "RUB", 0);
        var model = model();
        model.setUserId(null);
        model.setCustomerId("synthetic-user");
        assertEquals(0, aggregates.count(USER_ID, model, window, List.of()));
        assertEquals(0.0, aggregates.sum(USER_ID, model, window, List.of()));
        model.setCardToken("synthetic-legacy");
        assertEquals(1, aggregates.count(CARD_TOKEN, model, window, List.of()));
        model.setWalletId(null);
        assertEquals(0, aggregates.count(CARD_TOKEN, model, window, List.of(WALLET_ID)));
    }

    @Test
    void legacyAndEnrichedWithdrawalsCanBeWrittenThroughTheExistingBatchPath() {
        var withdrawal = new dev.vality.damsel.fraudbusters.Withdrawal()
                .setId("synthetic-batch").setEventTime("2026-01-01T12:00:00Z")
                .setCost(new dev.vality.damsel.domain.Cash(100, new dev.vality.damsel.domain.CurrencyRef("RUB")))
                .setStatus(dev.vality.damsel.fraudbusters.WithdrawalStatus.succeeded)
                .setAccount(new dev.vality.damsel.fraudbusters.Account()
                        .setId("synthetic-account").setCurrency(new dev.vality.damsel.domain.CurrencyRef("RUB")))
                .setDestinationResource(dev.vality.damsel.fraudbusters.Resource.digital_wallet(
                        new dev.vality.damsel.fraudbusters.DigitalWallet().setId("synthetic-destination")));
        var writer = new dev.vality.fraudbusters.repository.clickhouse.impl.WithdrawalRepositoryImpl(jdbc);
        writer.insertBatch(List.of(withdrawal));
        assertNull(jdbc.queryForObject("SELECT userId FROM fraud.withdrawal", String.class));
        withdrawal.setPartyId("party").setWalletId("wallet")
                .setClientInfo(new dev.vality.damsel.fraudbusters.ClientInfo()
                        .setCustomerId("synthetic-customer").setUserId("synthetic-user"));
        writer.insertBatch(List.of(withdrawal));
        assertEquals(1, aggregates.count(USER_ID, model(), window, List.of(PARTY_ID, WALLET_ID)));
    }

    private PayoutModel model() {
        var model = new PayoutModel();
        model.setUserId("synthetic-user");
        model.setPartyId("party");
        model.setWalletId("wallet");
        model.setCurrency("RUB");
        model.setAmount(999L);
        model.setTimestamp(timestamp + 2000);
        return model;
    }

    private void insert(String id, long amount, String status, String user, String party, String wallet,
                        String currency, int offset) {
        jdbc.update("""
                INSERT INTO fraud.withdrawal(timestamp, eventTime, eventTimeHour, id, amount, currency,
                                             status, cardToken, userId, partyId, walletId, errorCode)
                VALUES ('2026-01-01', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'synthetic-error')
                """, (timestamp + offset) / 1000, timestamp / 3600000 * 3600000, id, amount, currency, status,
                "synthetic-" + id, user, party, wallet);
    }
}
