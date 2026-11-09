package dev.vality.fraudbusters.repository.clickhouse.impl;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.CountryCode;
import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.domain.PaymentSystemRef;
import dev.vality.damsel.fraudbusters.Account;
import dev.vality.damsel.fraudbusters.ClientInfo;
import dev.vality.damsel.fraudbusters.DigitalWallet;
import dev.vality.damsel.fraudbusters.Error;
import dev.vality.damsel.fraudbusters.ProviderInfo;
import dev.vality.damsel.fraudbusters.Resource;
import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.damsel.fraudbusters.WithdrawalStatus;
import dev.vality.fraudbusters.constant.ClickhouseUtilsValue;
import dev.vality.fraudbusters.constant.FilterField;
import dev.vality.fraudbusters.constant.PaymentField;
import dev.vality.fraudbusters.constant.WithdrawalField;
import dev.vality.fraudbusters.constant.SortOrder;
import dev.vality.fraudbusters.extension.ClickHouseContainerExtension;
import dev.vality.fraudbusters.repository.clickhouse.mapper.WithdrawalMapper;
import dev.vality.fraudbusters.service.dto.FieldType;
import dev.vality.fraudbusters.service.dto.FilterDto;
import dev.vality.fraudbusters.service.dto.SearchFieldDto;
import dev.vality.fraudbusters.service.dto.SortDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.yandex.clickhouse.ClickHouseDataSource;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ClickHouseContainerExtension.class)
class HistoricalWithdrawalDataTest {

    private WithdrawalRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbc = new JdbcTemplate(new ClickHouseDataSource("jdbc:clickhouse://localhost:" +
                ClickHouseContainerExtension.CLICKHOUSE_CONTAINER.getFirstMappedPort() + "/default"));
        jdbc.execute("TRUNCATE TABLE fraud.withdrawal");
        repository = new WithdrawalRepositoryImpl(jdbc, new NamedParameterJdbcTemplate(jdbc), new WithdrawalMapper());
        // A payout is created before party and wallet are known, then succeeds.
        repository.insertBatch(List.of(
                card("w1", "2034-01-01T10:00:00Z", WithdrawalStatus.pending, null, null),
                card("w1", "2034-01-01T10:05:00Z", WithdrawalStatus.succeeded, "party-a", "wallet-a"),
                wallet("w2", "2034-01-01T10:10:00Z", "party-a", "wallet-a"),
                card("w3", "2034-01-01T10:20:00Z", WithdrawalStatus.succeeded, "party-b", "wallet-b")));
    }

    @Test
    void showsTheLatestStateOfEachPayoutOnce() {
        List<Withdrawal> withdrawals = repository.getByFilter(filter());

        assertEquals(List.of("w3", "w2", "w1"), withdrawals.stream().map(Withdrawal::getId).toList());
        Withdrawal first = withdrawals.get(2);
        assertEquals(WithdrawalStatus.succeeded, first.getStatus());
        assertEquals("2034-01-01T10:05:00Z", first.getEventTime());
        assertEquals("party-a", first.getPartyId());
        assertEquals("wallet-a", first.getWalletId());
        assertEquals(1000L, first.getCost().getAmount());
        assertEquals("RUB", first.getCost().getCurrency().getSymbolicCode());
        assertEquals("account-w1", first.getAccount().getId());
        BankCard card = first.getDestinationResource().getBankCard();
        assertEquals("token-w1", card.getToken());
        assertEquals("411111", card.getBin());
        assertEquals("1111", card.getLastDigits());
        assertEquals("VISA", card.getPaymentSystem().getId());
        assertEquals(CountryCode.RUS, card.getIssuerCountry());
        assertEquals("provider-1", first.getProviderInfo().getProviderId());
        assertEquals("terminal-1", first.getProviderInfo().getTerminalId());
        assertEquals("RUS", first.getProviderInfo().getCountry());
        assertEquals("user-w1", first.getClientInfo().getUserId());
        assertEquals("customer-w1", first.getClientInfo().getCustomerId());
        assertEquals("w1@example.com", first.getClientInfo().getEmail());
        assertFalse(first.isSetError());
    }

    @Test
    void keepsWhatIsKnownAboutOtherDestinations() {
        Withdrawal failed = repository.getByFilter(filter(search(PaymentField.ID, FieldType.STRING, "w2"))).get(0);

        assertEquals(WithdrawalStatus.failed, failed.getStatus());
        assertEquals(ClickhouseUtilsValue.UNKNOWN, failed.getDestinationResource().getGeneric().getId());
        assertEquals("insufficient_funds", failed.getError().getErrorCode());
        assertEquals("not enough money", failed.getError().getErrorReason());
        assertFalse(failed.isSetProviderInfo());
        assertFalse(failed.isSetClientInfo());
    }

    @Test
    void filtersApplyToTheLatestState() {
        assertEquals(List.of("w2", "w1"), ids(filter(search(WithdrawalField.WALLET_ID, FieldType.STRING, "wallet-a"))));
        assertEquals(List.of("w2"), ids(filter(search(PaymentField.STATUS, FieldType.ENUM, "failed"))));
        assertTrue(ids(filter(search(PaymentField.STATUS, FieldType.ENUM, "pending"))).isEmpty());
        assertEquals(List.of("w1"), ids(filter(
                search(PaymentField.PARTY_ID, FieldType.STRING, "party-a"),
                search(PaymentField.CARD_TOKEN, FieldType.STRING, "token-w1"))));
        assertEquals(List.of("w3"), ids(filter(search(PaymentField.USER_ID, FieldType.STRING, "user-w3"))));
    }

    @Test
    void periodDecidesWhichStateIsShown() {
        FilterDto beforeSuccess = filter();
        beforeSuccess.setTimeTo("2034-01-01T10:03:00");

        List<Withdrawal> withdrawals = repository.getByFilter(beforeSuccess);

        assertEquals(1, withdrawals.size());
        assertEquals(WithdrawalStatus.pending, withdrawals.get(0).getStatus());
        assertNull(withdrawals.get(0).getPartyId());
    }

    @Test
    void pagesThroughPayouts() {
        FilterDto firstPage = filter();
        firstPage.setSize(2L);
        assertEquals(List.of("w3", "w2"), ids(firstPage));

        FilterDto secondPage = filter();
        secondPage.setSize(2L);
        secondPage.setLastId("2");
        assertEquals(List.of("w1"), ids(secondPage));

        FilterDto ascending = filter();
        ascending.getSort().setOrder(SortOrder.ASC);
        assertEquals(List.of("w1", "w2", "w3"), ids(ascending));
    }

    private List<String> ids(FilterDto filter) {
        return repository.getByFilter(filter).stream().map(Withdrawal::getId).toList();
    }

    private static FilterDto filter(SearchFieldDto... searchFields) {
        FilterDto filter = new FilterDto();
        filter.setTimeFrom("2034-01-01T00:00:00");
        filter.setTimeTo("2034-01-02T00:00:00");
        SortDto sort = new SortDto();
        sort.setOrder(SortOrder.DESC);
        filter.setSort(sort);
        filter.setSearchFields(Set.of(searchFields));
        return filter;
    }

    private static SearchFieldDto search(FilterField field, FieldType type, String value) {
        return SearchFieldDto.builder()
                .field(field)
                .type(type)
                .value(value)
                .build();
    }

    private static Withdrawal card(String id, String time, WithdrawalStatus status, String party, String wallet) {
        return base(id, time, status, party, wallet)
                .setDestinationResource(Resource.bank_card(new BankCard()
                        .setToken("token-" + id)
                        .setBin("411111")
                        .setLastDigits("1111")
                        .setPaymentSystem(new PaymentSystemRef("VISA"))
                        .setIssuerCountry(CountryCode.RUS)))
                .setProviderInfo(new ProviderInfo()
                        .setProviderId("provider-1")
                        .setTerminalId("terminal-1")
                        .setCountry("RUS"))
                .setClientInfo(new ClientInfo()
                        .setUserId("user-" + id)
                        .setCustomerId("customer-" + id)
                        .setEmail(id + "@example.com"));
    }

    private static Withdrawal wallet(String id, String time, String party, String wallet) {
        return base(id, time, WithdrawalStatus.failed, party, wallet)
                .setDestinationResource(Resource.digital_wallet(new DigitalWallet().setId("destination-" + id)))
                .setError(new Error()
                        .setErrorCode("insufficient_funds")
                        .setErrorReason("not enough money"));
    }

    private static Withdrawal base(String id, String time, WithdrawalStatus status, String party, String wallet) {
        return new Withdrawal()
                .setId(id)
                .setEventTime(time)
                .setCost(new Cash(1000, new CurrencyRef("RUB")))
                .setStatus(status)
                .setAccount(new Account()
                        .setId("account-" + id)
                        .setCurrency(new CurrencyRef("RUB")))
                .setPartyId(party)
                .setWalletId(wallet);
    }
}
