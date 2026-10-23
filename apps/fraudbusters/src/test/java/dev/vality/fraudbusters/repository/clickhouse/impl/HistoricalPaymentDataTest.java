package dev.vality.fraudbusters.repository.clickhouse.impl;

import dev.vality.clickhouse.initializer.ChInitializer;
import dev.vality.fraudbusters.config.TestClickhouseConfig;
import dev.vality.fraudbusters.config.properties.ClickhouseProperties;
import dev.vality.fraudbusters.constant.FraudResultField;
import dev.vality.fraudbusters.constant.PaymentField;
import dev.vality.fraudbusters.constant.SortOrder;
import dev.vality.fraudbusters.domain.CheckedPayment;
import dev.vality.fraudbusters.extension.ClickHouseContainerExtension;
import dev.vality.fraudbusters.repository.Repository;
import dev.vality.fraudbusters.repository.clickhouse.mapper.CheckedPaymentMapper;
import dev.vality.fraudbusters.service.dto.FieldType;
import dev.vality.fraudbusters.service.dto.FilterDto;
import dev.vality.fraudbusters.service.dto.SearchFieldDto;
import dev.vality.fraudbusters.service.dto.SortDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ActiveProfiles("full-prod")
@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith({SpringExtension.class, ClickHouseContainerExtension.class})
@ContextConfiguration(classes = {
        ClickhouseProperties.class,
        TestClickhouseConfig.class,
        CheckedPaymentMapper.class,
        PaymentRepositoryImpl.class})
class HistoricalPaymentDataTest {

    @Autowired
    private Repository<CheckedPayment> paymentRepository;

    @BeforeAll
    static void setUp() throws Exception {
        ChInitializer.initAllScripts(ClickHouseContainerExtension.CLICKHOUSE_CONTAINER, List.of(
                "sql/data/insert_history_payments.sql",
                "sql/data/insert_history_fraud_results.sql",
                "sql/data/insert_recent_payment_rule_filter.sql"
        ));
    }

    @Test
    void aggregatesUseCustomersGloballyAndUsersWithinPartyOrShop() {
        var rows = List.of(
                identityPayment("a", "party-a", "shared-user", "customer-a", 100L),
                identityPayment("b", "party-b", "other-user", "customer-a", 200L),
                identityPayment("c", "party-b", "shared-user", "customer-b", 300L),
                identityPayment("d", "party-b", "fallback-user", null, 400L),
                identityPayment("e", "party-c", "fallback-user", "", 500L),
                identityPayment("f", "party-a", "shared-user", null, 700L));
        paymentRepository.insertBatch(rows);
        var resolver = new dev.vality.fraudbusters.fraud.payment.resolver.DatabasePaymentFieldResolver();
        var boundaries = new dev.vality.fraudbusters.service.TimeBoundaryServiceImpl();
        var repository = (dev.vality.fraudbusters.repository.PaymentRepository) paymentRepository;
        final var counts = new dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.CountAggregatorImpl(
                resolver, repository, null, null, boundaries);
        final var sums = new dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.SumAggregatorImpl(
                resolver, repository, null, null, boundaries);
        final var unique = new dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.UniqueValueAggregatorImpl(
                resolver, repository, boundaries);
        var model = new dev.vality.fraudbusters.fraud.model.PaymentModel();
        model.setTimestamp(java.time.Instant.parse("2032-01-01T01:00:00Z").toEpochMilli());
        model.setPartyId("party-a");
        model.setShopId("party-a-shop");
        model.setUserId("shared-user");
        model.setCustomerId("customer-a");
        model.setCardToken(rows.get(0).getCardToken());
        var window = dev.vality.fraudo.model.TimeWindow.builder().start(120)
                .timeUnit(dev.vality.fraudo.constant.TimeUnit.MINUTES).build();
        var user = dev.vality.fraudbusters.fraud.constant.PaymentCheckedField.USER_ID;
        var party = dev.vality.fraudbusters.fraud.constant.PaymentCheckedField.PARTY_ID;
        final var shop = dev.vality.fraudbusters.fraud.constant.PaymentCheckedField.SHOP_ID;
        final var card = dev.vality.fraudbusters.fraud.constant.PaymentCheckedField.CARD_TOKEN;
        assertEquals(2, counts.countSuccess(user, model, window, List.of()));
        assertEquals(300.0, sums.sumSuccess(user, model, window, List.of()));
        assertEquals(2, counts.countSuccess(user, model, window, List.of(party)));
        assertEquals(800.0, sums.sumSuccess(user, model, window, List.of(party)));
        assertEquals(2, counts.countSuccess(user, model, window, List.of(shop)));
        assertEquals(800.0, sums.sumSuccess(user, model, window, List.of(shop)));
        assertEquals(2, counts.countSuccess(shop, model, window, List.of(user)));
        assertEquals(2, counts.countSuccess(card, model, window, List.of(user)));
        assertEquals(2, counts.countSuccess(party, model, window, List.of(user)));
        assertEquals(5, unique.countUniqueValue(card, model, user, window, List.of()));
        assertEquals(2, unique.countUniqueValue(card, model, user, window, List.of(party)));
        assertEquals(2, unique.countUniqueValue(card, model, user, window, List.of(shop)));
        assertEquals(2, unique.countUniqueValue(shop, model, user, window, List.of()));
        assertEquals(2, unique.countUniqueValue(party, model, user, window, List.of()));
        model.setCustomerId(null);
        model.setUserId("fallback-user");
        assertEquals(2, counts.countSuccess(user, model, window, List.of()));
        assertEquals(900.0, sums.sumSuccess(user, model, window, List.of()));
        model.setCustomerId("customer-a");
        model.setUserId(null);
        assertEquals(2, counts.countSuccess(user, model, window, List.of()));
        assertEquals(0, counts.countSuccess(user, model, window, List.of(party)));
    }

    private CheckedPayment identityPayment(String suffix, String party, String user, String customer, long amount) {
        var context = dev.vality.fraudbusters.util.BeanUtil.createContext();
        CheckedPayment payment = dev.vality.fraudbusters.util.BeanUtil.convertContextToPayment(context, "captured");
        var time = dev.vality.fraudbusters.util.TimestampUtil.generateTimePropertiesByString("2032-01-01T00:00:00");
        payment.setTimestamp(time.getTimestamp());
        payment.setEventTime(time.getEventTime());
        payment.setEventTimeHour(time.getEventTimeHour());
        payment.setId("synthetic-scope-" + suffix);
        payment.setPartyId(party);
        payment.setShopId(party + "-shop");
        payment.setUserId(user);
        payment.setCustomerId(customer);
        payment.setAmount(amount);
        return payment;
    }

    @Test
    void clientIdentitySurvivesWriteAndHistoryRead() {
        var context = dev.vality.fraudbusters.util.BeanUtil.createContext();
        CheckedPayment payment = dev.vality.fraudbusters.util.BeanUtil.convertContextToPayment(context, "captured");
        var time = dev.vality.fraudbusters.util.TimestampUtil.generateTimePropertiesByString("2031-01-01T00:00:00");
        payment.setTimestamp(time.getTimestamp());
        payment.setEventTime(time.getEventTime());
        payment.setEventTimeHour(time.getEventTimeHour());
        payment.setId("synthetic-identity-payment");
        payment.setCustomerId("synthetic-customer");
        payment.setUserId("Synthetic_'User%");
        paymentRepository.insertBatch(List.of(payment));
        FilterDto filter = new FilterDto();
        filter.setTimeFrom("2031-01-01T00:00:00");
        filter.setTimeTo("2031-01-02T00:00:00");
        SortDto sort = new SortDto();
        sort.setOrder(SortOrder.DESC);
        filter.setSort(sort);
        filter.setSearchFields(Set.of(SearchFieldDto.builder().field(PaymentField.USER_ID)
                .type(FieldType.STRING).value(payment.getUserId()).build()));
        var rows = paymentRepository.getByFilter(filter);
        assertEquals(1, rows.size());
        assertEquals(payment.getCustomerId(), rows.get(0).getCustomerId());
        assertEquals(payment.getUserId(), rows.get(0).getUserId());
    }

    @Test
    void getPaymentsByTimeSlot() {
        FilterDto filter = new FilterDto();
        filter.setTimeFrom("2020-05-01T18:04:53");
        filter.setTimeTo("2020-10-01T18:04:53");
        SortDto sortDto = new SortDto();
        sortDto.setOrder(SortOrder.DESC);
        filter.setSort(sortDto);

        List<CheckedPayment> payments = paymentRepository.getByFilter(filter);

        assertFalse(payments.isEmpty());
        assertEquals(6, payments.size());
    }

    @Test
    void getPaymentsByTimeSlotAndSearchPatterns() {
        FilterDto filter = new FilterDto();
        filter.setTimeFrom("2020-05-01T18:04:53");
        filter.setTimeTo("2020-10-01T18:04:53");
        Set<SearchFieldDto> searchFields = new HashSet<>();
        searchFields.add(SearchFieldDto.builder()
                .field(PaymentField.PARTY_ID)
                .type(FieldType.STRING)
                .value("partyId_2")
                .build());
        searchFields.add(SearchFieldDto.builder()
                .field(PaymentField.SHOP_ID)
                .type(FieldType.STRING)
                .value("2035728")
                .build());
        filter.setSearchFields(searchFields);
        SortDto sortDto = new SortDto();
        sortDto.setOrder(SortOrder.DESC);
        filter.setSort(sortDto);

        List<CheckedPayment> payments = paymentRepository.getByFilter(filter);

        assertFalse(payments.isEmpty());
        assertEquals(1, payments.size());
        assertEquals("2035728", payments.get(0).getShopId());
        assertEquals("partyId_2", payments.get(0).getPartyId());
    }

    @Test
    void getPaymentsByEmailTemplateAndRule() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        FilterDto filter = new FilterDto();
        filter.setTimeFrom(today.minusDays(1) + "T00:00:00");
        filter.setTimeTo(today.plusDays(1) + "T00:00:00");
        filter.setSearchFields(Set.of(
                SearchFieldDto.builder()
                        .field(PaymentField.EMAIL)
                        .type(FieldType.STRING)
                        .value("rule_filter_email")
                        .build(),
                SearchFieldDto.builder()
                        .field(FraudResultField.CHECKED_TEMPLATE)
                        .type(FieldType.STRING)
                        .value("3DS_TEMPLATE")
                        .build(),
                SearchFieldDto.builder()
                        .field(FraudResultField.CHECKED_RULE)
                        .type(FieldType.STRING)
                        .value("3DS_RULE")
                        .build()
        ));
        SortDto sortDto = new SortDto();
        sortDto.setOrder(SortOrder.DESC);
        filter.setSort(sortDto);

        List<CheckedPayment> payments = paymentRepository.getByFilter(filter);

        assertEquals(1, payments.size());
        assertEquals("rule-filter-payment.1", payments.get(0).getId());
    }

    @Test
    void getPaymentsByTimeSlotAndLimitSize() {
        FilterDto filter = new FilterDto();
        filter.setTimeFrom("2020-05-01T18:04:53");
        filter.setTimeTo("2020-10-01T18:04:53");
        filter.setSize(3L);
        SortDto sortDto = new SortDto();
        sortDto.setOrder(SortOrder.DESC);
        filter.setSort(sortDto);

        List<CheckedPayment> payments = paymentRepository.getByFilter(filter);

        assertFalse(payments.isEmpty());
        assertEquals(3, payments.size());
    }

    @Test
    void getPaymentsByTimeSlotAndPageAndSort() {
        FilterDto filter = new FilterDto();
        filter.setTimeFrom("2020-05-01T18:04:53");
        filter.setTimeTo("2020-10-01T18:04:53");
        filter.setLastId("3");
        SortDto sortDto = new SortDto();
        sortDto.setOrder(SortOrder.DESC);
        filter.setSort(sortDto);

        List<CheckedPayment> payments = paymentRepository.getByFilter(filter);

        assertFalse(payments.isEmpty());
        assertEquals(3, payments.size());
    }

    @Test
    void getPaymentsByTimeSlotAndPageAndSearchPatternsAndSort() {
        FilterDto filter = new FilterDto();
        filter.setTimeFrom("2020-05-01T18:04:53");
        filter.setTimeTo("2020-10-01T18:04:53");
        filter.setSize(3L);
        filter.setLastId("1");
        SortDto sortDto = new SortDto();
        sortDto.setOrder(SortOrder.DESC);
        filter.setSort(sortDto);
        Set<SearchFieldDto> searchFields = new HashSet<>();
        searchFields.add(SearchFieldDto.builder()
                .field(PaymentField.PARTY_ID)
                .type(FieldType.STRING)
                .value("partyId_2")
                .build());
        filter.setSearchFields(searchFields);

        List<CheckedPayment> payments = paymentRepository.getByFilter(filter);

        assertFalse(payments.isEmpty());
        assertEquals(1, payments.size());
        assertEquals("partyId_2", payments.get(0).getPartyId());
    }

    @Test
    void getPaymentsByTimeSlotAndSearchByStatusEnum() {
        FilterDto filter = new FilterDto();
        filter.setTimeFrom("2020-05-01T18:04:53");
        filter.setTimeTo("2020-10-01T18:04:53");
        SortDto sortDto = new SortDto();
        sortDto.setOrder(SortOrder.DESC);
        filter.setSort(sortDto);
        Set<SearchFieldDto> searchFields = new HashSet<>();
        String filterStatus = "captured";
        searchFields.add(SearchFieldDto.builder()
                .field(PaymentField.STATUS)
                .type(FieldType.ENUM)
                .value(filterStatus)
                .build());
        filter.setSearchFields(searchFields);

        List<CheckedPayment> payments = paymentRepository.getByFilter(filter);

        assertFalse(payments.isEmpty());
        assertEquals(2, payments.size());
        assertEquals(filterStatus, payments.get(0).getPaymentStatus());
        assertEquals(filterStatus, payments.get(1).getPaymentStatus());
    }

}
