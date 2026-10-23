package dev.vality.fraudbusters.fraud.aggragator;

import dev.vality.fraudbusters.domain.CheckedPayment;
import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.filter.PaymentFieldValueFilter;
import dev.vality.fraudbusters.fraud.filter.PaymentFieldValueResolver;
import dev.vality.fraudbusters.fraud.localstorage.LocalPaymentFieldResolver;
import dev.vality.fraudbusters.fraud.localstorage.LocalResultStorage;
import dev.vality.fraudbusters.fraud.localstorage.LocalResultStorageRepository;
import dev.vality.fraudbusters.fraud.localstorage.aggregator.LocalCountAggregatorDecorator;
import dev.vality.fraudbusters.fraud.localstorage.aggregator.LocalSumAggregatorDecorator;
import dev.vality.fraudbusters.fraud.localstorage.aggregator.LocalUniqueValueAggregatorDecorator;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.CountAggregatorImpl;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.SumAggregatorImpl;
import dev.vality.fraudbusters.fraud.payment.aggregator.clickhouse.UniqueValueAggregatorImpl;
import dev.vality.fraudbusters.fraud.payment.resolver.DatabasePaymentFieldResolver;
import dev.vality.fraudbusters.repository.AggregationRepository;
import dev.vality.fraudbusters.repository.PaymentRepository;
import dev.vality.fraudbusters.service.TimeBoundaryServiceImpl;
import dev.vality.fraudo.model.TimeWindow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.vality.fraudbusters.fraud.constant.PaymentCheckedField.*;
import static dev.vality.fraudo.constant.TimeUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserIdentityAggregationTest {

    private final DatabasePaymentFieldResolver resolver = new DatabasePaymentFieldResolver();
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final AggregationRepository refunds = mock(AggregationRepository.class);
    private final AggregationRepository chargebacks = mock(AggregationRepository.class);
    private final TimeBoundaryServiceImpl boundaries = new TimeBoundaryServiceImpl();
    private final TimeWindow window = TimeWindow.builder().start(60).timeUnit(MINUTES).build();

    private PaymentModel model() {
        PaymentModel model = new PaymentModel();
        model.setCustomerId("synthetic-customer");
        model.setUserId("Synthetic-User");
        model.setAmount(100L);
        return model;
    }

    @Test
    void userIdIsIndependentOfCustomerId() {
        PaymentModel model = model();
        assertEquals("userId", resolver.resolve(USER_ID, model).getName());
        assertEquals("Synthetic-User", resolver.resolve(USER_ID, model).getValue());
        model.setCustomerId(null);
        assertEquals("Synthetic-User", resolver.resolve(USER_ID, model).getValue());
        model.setCustomerId("");
        assertEquals("Synthetic-User", resolver.resolve(USER_ID, model).getValue());
        model.setCustomerId("synthetic-customer");
        model.setUserId(null);
        assertNull(resolver.resolve(USER_ID, model).getValue());
    }

    @Test
    void groupedFieldsAlwaysUseUserId() {
        PaymentModel model = model();
        for (var scope : List.of(PARTY_ID, SHOP_ID, CARD_TOKEN)) {
            var group = resolver.resolveListFields(model, List.of(scope, USER_ID)).get(1);
            assertEquals("userId", group.getName());
            assertEquals("Synthetic-User", group.getValue());
        }
    }

    @Test
    void everyCountPathUsesUserId() {
        var counts = new CountAggregatorImpl(resolver, payments, refunds, chargebacks, boundaries);
        PaymentModel model = model();
        counts.count(USER_ID, model, window, List.of());
        counts.countSuccess(USER_ID, model, window, List.of());
        counts.countPending(USER_ID, model, window, List.of());
        counts.countError(USER_ID, model, window, List.of());
        counts.countError(USER_ID, model, window, "synthetic-error", List.of());
        counts.countRefund(USER_ID, model, window, List.of());
        counts.countChargeback(USER_ID, model, window, List.of());
        verify(payments, times(2)).countOperationByFieldWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()));
        verify(payments).countOperationSuccessWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()));
        verify(payments).countOperationPendingWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()));
        verify(payments).countOperationErrorWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()));
        verify(payments).countOperationErrorWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()), eq("synthetic-error"));
        verify(refunds).countOperationByFieldWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()));
        verify(chargebacks).countOperationByFieldWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()));
    }

    @Test
    void everySumPathUsesUserId() {
        var sums = new SumAggregatorImpl(resolver, payments, refunds, chargebacks, boundaries);
        PaymentModel model = model();
        sums.sum(USER_ID, model, window, List.of());
        sums.sumSuccess(USER_ID, model, window, List.of());
        sums.sumError(USER_ID, model, window, List.of());
        sums.sumError(USER_ID, model, window, "synthetic-error", List.of());
        sums.sumRefund(USER_ID, model, window, List.of());
        sums.sumChargeback(USER_ID, model, window, List.of());
        verify(payments).sumOperationByFieldWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()));
        verify(payments).sumOperationSuccessWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()));
        verify(payments).sumOperationErrorWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()));
        verify(payments).sumOperationErrorWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()), eq("synthetic-error"));
        verify(refunds).sumOperationByFieldWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()));
        verify(chargebacks).sumOperationByFieldWithGroupBy(eq("userId"), eq("Synthetic-User"),
                any(), any(), eq(List.of()));
    }

    @Test
    void historicalEmulationUsesOnlyUserId() {
        var storage = new LocalResultStorage();
        var values = new PaymentFieldValueResolver();
        var repository = new LocalResultStorageRepository(storage, new PaymentFieldValueFilter(values), values);
        var localResolver = new LocalPaymentFieldResolver(resolver);
        final var counts = new LocalCountAggregatorDecorator(mock(CountAggregatorImpl.class), localResolver,
                repository, boundaries);
        final var sums = new LocalSumAggregatorDecorator(mock(SumAggregatorImpl.class), localResolver,
                repository, boundaries);
        final var unique = new LocalUniqueValueAggregatorDecorator(mock(UniqueValueAggregatorImpl.class), localResolver,
                repository, boundaries);
        storage.get().addAll(List.of(
                localPayment("party-a", "shared-user", "customer-a", 100L),
                localPayment("party-b", "other-user", "customer-a", 200L),
                localPayment("party-b", "shared-user", "customer-b", 300L),
                localPayment("party-b", "fallback-user", null, 400L),
                localPayment("party-c", "fallback-user", "", 500L),
                localPayment("party-a", "shared-user", null, 700L),
                localPayment("party-c", null, null, 800L)));
        PaymentModel model = model();
        model.setUserId("shared-user");
        model.setCustomerId("customer-a");
        model.setPartyId("party-a");
        model.setShopId("party-a-shop");
        model.setCardToken("synthetic-card");
        model.setAmount(0L);
        assertEquals(3, counts.count(USER_ID, model, window, List.of()));
        assertEquals(2, counts.countSuccess(USER_ID, model, window, List.of(PARTY_ID)));
        assertEquals(3, counts.countSuccess(USER_ID, model, window, List.of()));
        assertEquals(1100.0, sums.sumSuccess(USER_ID, model, window, List.of()));
        assertEquals(800.0, sums.sumSuccess(USER_ID, model, window, List.of(PARTY_ID)));
        assertEquals(2, counts.countSuccess(USER_ID, model, window, List.of(SHOP_ID)));
        assertEquals(800.0, sums.sumSuccess(USER_ID, model, window, List.of(SHOP_ID)));
        assertEquals(2, counts.countSuccess(SHOP_ID, model, window, List.of(USER_ID)));
        assertEquals(3, counts.countSuccess(CARD_TOKEN, model, window, List.of(USER_ID)));
        assertEquals(2, counts.countSuccess(PARTY_ID, model, window, List.of(USER_ID)));
        assertEquals(3, unique.countUniqueValue(CARD_TOKEN, model, USER_ID, window, List.of()));
        assertEquals(1, unique.countUniqueValue(CARD_TOKEN, model, USER_ID, window, List.of(PARTY_ID)));
        assertEquals(1, unique.countUniqueValue(CARD_TOKEN, model, USER_ID, window, List.of(SHOP_ID)));
        assertEquals(1, unique.countUniqueValue(SHOP_ID, model, USER_ID, window, List.of()));
        model.setCustomerId("different-customer");
        assertEquals(3, counts.countSuccess(USER_ID, model, window, List.of()));
        assertEquals(1100.0, sums.sumSuccess(USER_ID, model, window, List.of()));
        model.setUserId(null);
        assertEquals(0, counts.countSuccess(USER_ID, model, window, List.of()));
        assertEquals(0.0, sums.sumSuccess(USER_ID, model, window, List.of()));
        model.setUserId("fallback-user");
        assertEquals(2, counts.countSuccess(USER_ID, model, window, List.of()));
        assertEquals(900.0, sums.sumSuccess(USER_ID, model, window, List.of()));
    }

    private CheckedPayment localPayment(String party, String user, String customer, long amount) {
        var payment = new CheckedPayment();
        payment.setEventTime(java.time.Instant.now().getEpochSecond() - 60);
        payment.setPartyId(party);
        payment.setShopId(party + "-shop");
        payment.setUserId(user);
        payment.setCustomerId(customer);
        payment.setAmount(amount);
        payment.setCardToken("synthetic-card");
        payment.setPaymentStatus("captured");
        return payment;
    }

    @Test
    void uniqueResolvesBothSelectionAndCountedField() {
        var unique = new UniqueValueAggregatorImpl(resolver, payments, boundaries);
        PaymentModel model = model();
        model.setPartyId("synthetic-party");
        unique.countUniqueValue(USER_ID, model, USER_ID, window, List.of());
        verify(payments).uniqCountOperationWithGroupBy(eq("userId"), eq("Synthetic-User"), eq("userId"),
                any(), any(), eq(List.of()));
        unique.countUniqueValue(USER_ID, model, USER_ID, window, List.of(PARTY_ID));
        verify(payments).uniqCountOperationWithGroupBy(eq("userId"), eq("Synthetic-User"), eq("userId"),
                any(), any(), eq(List.of(new FieldModel("partyId", "synthetic-party"))));
    }
}
