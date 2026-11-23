package dev.vality.fraudbusters.repository.clickhouse.impl;

import dev.vality.fraudbusters.constant.FilterField;
import dev.vality.fraudbusters.constant.SortOrder;
import dev.vality.fraudbusters.constant.PayoutCheckField;
import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.domain.ConcreteResultModel;
import dev.vality.fraudbusters.domain.PayoutResult;
import dev.vality.fraudbusters.extension.ClickHouseContainerExtension;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.repository.clickhouse.mapper.PayoutCheckMapper;
import dev.vality.fraudbusters.service.dto.FieldType;
import dev.vality.fraudbusters.service.dto.FilterDto;
import dev.vality.fraudbusters.service.dto.SearchFieldDto;
import dev.vality.fraudbusters.service.dto.SortDto;
import dev.vality.fraudo.constant.ResultStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(ClickHouseContainerExtension.class)
class PayoutCheckRepositoryTest {

    private PayoutCheckRepository repository;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbc = new JdbcTemplate(ClickHouseContainerExtension.createDataSource());
        jdbc.execute("TRUNCATE TABLE fraud.payout_check");
        repository = new PayoutCheckRepository(jdbc, new NamedParameterJdbcTemplate(jdbc),
                new PayoutCheckMapper());
        repository.insertBatch(List.of(
                card("w1", "2034-01-01T10:00:00Z", "wallet-a", ResultStatus.ACCEPT, "rest"),
                card("w2", "2034-01-01T10:10:00Z", "wallet-a", ResultStatus.DECLINE, "card_velocity"),
                notChecked("w3", "2034-01-01T10:20:00Z", "wallet-b")));
    }

    @Test
    void readsBackTheCheckedPayoutAndItsResult() {
        List<PayoutResult> results = repository.getByFilter(filter());

        assertEquals(List.of("w3", "w2", "w1"), ids(results));
        PayoutResult declined = results.get(1);
        PayoutModel model = declined.getPayoutModel();
        assertEquals(Instant.parse("2034-01-01T10:10:00Z").toEpochMilli(), model.getTimestamp());
        assertEquals("party", model.getPartyId());
        assertEquals("wallet-a", model.getWalletId());
        assertEquals(1000L, model.getAmount());
        assertEquals("RUB", model.getCurrency());
        assertEquals("token-w2", model.getCardToken());
        assertEquals("411111", model.getBin());
        assertEquals("1111", model.getLastDigits());
        assertEquals("VISA", model.getPaymentSystem());
        assertEquals("RUS", model.getBinCountryCode());
        assertEquals("user-w2", model.getUserId());
        assertEquals("w2@example.com", model.getEmail());
        assertEquals(ResultStatus.DECLINE, declined.getResultModel().getResultModel().getResultStatus());
        assertEquals("card_velocity", declined.getResultModel().getResultModel().getRuleChecked());
        assertEquals("template", declined.getResultModel().getCheckedTemplate());

        PayoutResult notChecked = results.get(0);
        assertNull(notChecked.getPayoutModel().getCardToken());
        assertNull(notChecked.getPayoutModel().getUserId());
        assertNull(notChecked.getResultModel().getResultModel().getRuleChecked());
        assertEquals("RULE_NOT_CHECKED", notChecked.getResultModel().getCheckedTemplate());
    }

    @Test
    void filtersByResultRuleAndPayoutFields() {
        assertEquals(List.of("w2"), ids(repository.getByFilter(filter(
                search(PayoutCheckField.RESULT_STATUS, FieldType.ENUM, ResultStatus.DECLINE.name())))));
        assertEquals(List.of("w1"), ids(repository.getByFilter(filter(
                search(PayoutCheckField.CHECKED_RULE, FieldType.ENUM, "rest")))));
        assertEquals(List.of("w2", "w1"), ids(repository.getByFilter(filter(
                search(PayoutCheckField.WALLET_ID, FieldType.STRING, "wallet-a")))));
        assertEquals(List.of("w2"), ids(repository.getByFilter(filter(
                search(PayoutCheckField.USER_ID, FieldType.STRING, "user-w2"),
                search(PayoutCheckField.PARTY_ID, FieldType.STRING, "party")))));
        assertEquals(List.of("w3"), ids(repository.getByFilter(filter(
                search(PayoutCheckField.ID, FieldType.STRING, "w3")))));
    }

    @Test
    void pagesThroughResults() {
        FilterDto firstPage = filter();
        firstPage.setSize(2L);
        assertEquals(List.of("w3", "w2"), ids(repository.getByFilter(firstPage)));

        FilterDto secondPage = filter();
        secondPage.setSize(2L);
        secondPage.setLastId("2");
        assertEquals(List.of("w1"), ids(repository.getByFilter(secondPage)));

        FilterDto outside = filter();
        outside.setTimeTo("2034-01-01T10:05:00");
        assertEquals(List.of("w1"), ids(repository.getByFilter(outside)));
    }

    private static List<String> ids(List<PayoutResult> results) {
        return results.stream()
                .map(result -> result.getPayoutModel().getPayoutId())
                .toList();
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

    private static PayoutResult card(String id, String time, String wallet, ResultStatus status, String rule) {
        PayoutModel model = model(id, time, wallet);
        model.setCardToken("token-" + id);
        model.setBin("411111");
        model.setLastDigits("1111");
        model.setPaymentSystem("VISA");
        model.setBinCountryCode("RUS");
        model.setUserId("user-" + id);
        model.setCustomerId("customer-" + id);
        model.setEmail(id + "@example.com");
        CheckedResultModel result = new CheckedResultModel();
        result.setCheckedTemplate("template");
        result.setResultModel(new ConcreteResultModel(status, rule, null));
        return new PayoutResult(model, result);
    }

    private static PayoutResult notChecked(String id, String time, String wallet) {
        CheckedResultModel result = new CheckedResultModel();
        result.setCheckedTemplate("RULE_NOT_CHECKED");
        result.setResultModel(new ConcreteResultModel(ResultStatus.HIGH_RISK, null, null));
        return new PayoutResult(model(id, time, wallet), result);
    }

    private static PayoutModel model(String id, String time, String wallet) {
        PayoutModel model = new PayoutModel();
        model.setPayoutId(id);
        model.setTimestamp(Instant.parse(time).toEpochMilli());
        model.setPartyId("party");
        model.setWalletId(wallet);
        model.setAmount(1000L);
        model.setCurrency("RUB");
        return model;
    }
}
