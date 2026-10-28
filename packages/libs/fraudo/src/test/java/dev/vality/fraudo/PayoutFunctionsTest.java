package dev.vality.fraudo;

import dev.vality.fraudo.aggregator.UniqueValueAggregator;
import dev.vality.fraudo.bundle.AggregatorBundle;
import dev.vality.fraudo.bundle.FinderBundle;
import dev.vality.fraudo.bundle.ResolverBundle;
import dev.vality.fraudo.bundle.VisitorBundle;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.exception.UnresolvableFieldException;
import dev.vality.fraudo.model.BaseModel;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.model.TimeWindow;
import dev.vality.fraudo.payment.aggregator.CountPaymentAggregator;
import dev.vality.fraudo.payment.aggregator.SumPaymentAggregator;
import dev.vality.fraudo.payment.factory.FraudVisitorFactory;
import dev.vality.fraudo.payment.factory.FraudVisitorFactoryImpl;
import dev.vality.fraudo.payment.factory.FullVisitorFactoryImpl;
import dev.vality.fraudo.resolver.impl.GroupFieldsResolverImpl;
import dev.vality.fraudo.resolver.impl.TimeWindowResolverImpl;
import dev.vality.fraudo.payout.aggregator.CountPayoutAggregator;
import dev.vality.fraudo.payout.aggregator.SumPayoutAggregator;
import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.utils.ResultUtils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayoutFunctionsTest {

    @Mock
    private CountPayoutAggregator<BaseModel, String> payouts;
    @Mock
    private SumPayoutAggregator<BaseModel, String> payoutSums;
    @Mock
    private UniqueValueAggregator<BaseModel, String> payoutUnique;
    @Mock
    private CountPaymentAggregator<BaseModel, String> payments;
    @Mock
    private SumPaymentAggregator<BaseModel, String> paymentSums;
    @Mock
    private UniqueValueAggregator<BaseModel, String> paymentUnique;

    private final BaseModel model = new BaseModel();
    private final TimeWindow window = TimeWindow.builder().start(24).timeUnit("hours").build();
    private final FieldResolver<BaseModel, String> fields = new FieldResolver<>() {
        @Override
        public String resolveValue(String fieldName, BaseModel value) {
            return value.getUserId();
        }

        @Override
        public String resolveName(String fieldName) {
            if (!List.of("user_id", "party_id", "wallet_id", "currency", "card_token").contains(fieldName)) {
                throw new UnresolvableFieldException(fieldName);
            }
            return fieldName;
        }
    };

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void routesEveryPayoutFunctionToPayoutHistory(boolean fullVisitor) {
        var scope = List.of("party_id", "currency");
        when(payouts.count("user_id", model, window, scope)).thenReturn(2);
        when(payouts.countSuccess("user_id", model, window, scope)).thenReturn(1);
        when(payouts.countError("user_id", model, window, "failed", scope)).thenReturn(1);
        when(payoutSums.sum("user_id", model, window, scope)).thenReturn(200.0);
        when(payoutSums.sumSuccess("user_id", model, window, scope)).thenReturn(100.0);
        when(payoutSums.sumError("user_id", model, window, "failed", scope)).thenReturn(100.0);
        when(payoutUnique.countUniqueValue("user_id", model, "card_token", window, scope)).thenReturn(2);

        var result = evaluate("""
                rule: countPayouts("user_id", 24, hours, "party_id", "currency") = 2
                  AND countPayoutsSuccess("user_id", 24, hours, "party_id", "currency") = 1
                  AND countPayoutsError("user_id", 24, hours, "failed", "party_id", "currency") = 1
                  AND sumPayouts("user_id", 24, hours, "party_id", "currency") = 200
                  AND sumPayoutsSuccess("user_id", 24, hours, "party_id", "currency") = 100
                  AND sumPayoutsError("user_id", 24, hours, "failed", "party_id", "currency") = 100
                  AND uniquePayouts("user_id", "card_token", 24, hours, "party_id", "currency") = 2 -> decline;
                """, fullVisitor, payouts);

        assertStatus(ResultStatus.DECLINE, result);
        verify(payoutUnique).countUniqueValue("user_id", model, "card_token", window, scope);
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void preservesFirstMatchAndFullTraversalWithPayoutFunctions(boolean fullVisitor) {
        when(payouts.count("user_id", model, window, List.of())).thenReturn(2);

        var result = evaluate("""
                rule: countPayouts("user_id", 24, hours) > 0 -> decline;
                rule: countPayouts("user_id", 24, hours) > 1 -> highRisk;
                """, fullVisitor, payouts);

        assertEquals(fullVisitor ? 2 : 1, result.getRuleResults().size());
        assertEquals(ResultStatus.DECLINE, result.getRuleResults().get(0).getResultStatus());
        if (fullVisitor) {
            assertEquals(ResultStatus.HIGH_RISK, result.getRuleResults().get(1).getResultStatus());
        }
        verify(payouts).count("user_id", model, window, List.of());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void allowsThreeDsForPaymentRuleUsingPayoutHistory(boolean fullVisitor) {
        when(payments.countSuccess("user_id", model, window, List.of())).thenReturn(0);
        when(payouts.count("user_id", model, window, List.of())).thenReturn(2);

        assertStatus(ResultStatus.THREE_DS, evaluate("""
                rule: countSuccess("user_id", 24, hours) = 0
                  AND countPayouts("user_id", 24, hours) > 1 -> 3ds;
                """, fullVisitor, payouts));

        verify(payments).countSuccess("user_id", model, window, List.of());
        verify(payouts).count("user_id", model, window, List.of());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void allowsThreeDsCatchForPaymentRuleWhenPayoutHistoryFails(boolean fullVisitor) {
        when(payments.countSuccess("user_id", model, window, List.of())).thenReturn(0);
        when(payouts.count("user_id", model, window, List.of()))
                .thenThrow(new IllegalStateException("History unavailable"));

        assertStatus(ResultStatus.THREE_DS, evaluate("""
                rule: countSuccess("user_id", 24, hours) = 0
                  AND countPayouts("user_id", 24, hours) > 1 -> accept catch: 3ds;
                """, fullVisitor, payouts));

        verify(payments).countSuccess("user_id", model, window, List.of());
        verify(payouts).count("user_id", model, window, List.of());
    }

    @Test
    void supportsErrorFunctionsWithoutCode() {
        when(payouts.countError("user_id", model, window, List.of())).thenReturn(1);
        when(payoutSums.sumError("user_id", model, window, List.of())).thenReturn(100.0);
        assertStatus(ResultStatus.DECLINE, evaluate("""
                rule: countPayoutsError("user_id", 24, hours) = 1
                  AND sumPayoutsError("user_id", 24, hours) = 100 -> decline;
                """, false, payouts));
    }

    @Test
    void separatesPaymentAndPayoutFunctionsAndCachesRepeatedPayoutCalls() {
        when(payments.countSuccess("user_id", model, window, List.of("party_id"))).thenReturn(0);
        when(payouts.count("user_id", model, window, List.of("party_id"))).thenReturn(2);
        assertStatus(ResultStatus.DECLINE, evaluate("""
                rule: countSuccess("user_id", 24, hours, "party_id") = 0
                  AND countPayouts("user_id", 24, hours, "party_id") > 0
                  AND countPayouts("user_id", 24, hours, "party_id") < 3 -> decline;
                """, false, payouts));
        verify(payouts).count("user_id", model, window, List.of("party_id"));
        verify(payments).countSuccess("user_id", model, window, List.of("party_id"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void combinesSuccessfulPaymentsAndPayoutsWithAnd(boolean fullVisitor) {
        var paymentWindow = TimeWindow.builder().start(30).timeUnit("days").build();
        var scope = List.of("party_id");
        when(payments.countSuccess("user_id", model, paymentWindow, scope)).thenReturn(0, 1, 0);
        when(payouts.countSuccess("user_id", model, window, scope)).thenReturn(4, 3);
        String template = """
                rule: countSuccess("user_id", 30, days, "party_id") = 0
                  AND countPayoutsSuccess("user_id", 24, hours, "party_id") > 3 -> decline;
                rule: 1 = 1 -> accept;
                """;

        assertStatus(ResultStatus.DECLINE, evaluate(template, fullVisitor, payouts));
        assertStatus(ResultStatus.ACCEPT, evaluate(template, fullVisitor, payouts));
        assertStatus(ResultStatus.ACCEPT, evaluate(template, fullVisitor, payouts));
        verify(payments, times(3)).countSuccess("user_id", model, paymentWindow, scope);
        verify(payouts, times(2)).countSuccess("user_id", model, window, scope);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void combinesSuccessfulPaymentsAndPayoutsWithOrNotAndParentheses(boolean fullVisitor) {
        var scope = List.of("party_id");
        when(payments.countSuccess("user_id", model, window, scope)).thenReturn(0, 0, 1);
        when(payouts.countSuccess("user_id", model, window, scope)).thenReturn(2, 4);
        String template = """
                rule: NOT (countSuccess("user_id", 24, hours, "party_id") > 0
                  OR countPayoutsSuccess("user_id", 24, hours, "party_id") > 3) -> decline;
                rule: 1 = 1 -> accept;
                """;

        assertStatus(ResultStatus.DECLINE, evaluate(template, fullVisitor, payouts));
        assertStatus(ResultStatus.ACCEPT, evaluate(template, fullVisitor, payouts));
        assertStatus(ResultStatus.ACCEPT, evaluate(template, fullVisitor, payouts));
        verify(payments, times(3)).countSuccess("user_id", model, window, scope);
        verify(payouts, times(2)).countSuccess("user_id", model, window, scope);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void comparesSuccessfulPayoutCountDirectlyToSuccessfulPaymentCount(boolean fullVisitor) {
        var scope = List.of("party_id");
        when(payouts.countSuccess("user_id", model, window, scope)).thenReturn(4, 1, 2);
        when(payments.countSuccess("user_id", model, window, scope)).thenReturn(3, 2, 2);
        String template = """
                rule: countPayoutsSuccess("user_id", 24, hours, "party_id")
                  > countSuccess("user_id", 24, hours, "party_id") -> highRisk;
                rule: 1 = 1 -> accept;
                """;

        assertStatus(ResultStatus.HIGH_RISK, evaluate(template, fullVisitor, payouts));
        assertStatus(ResultStatus.ACCEPT, evaluate(template, fullVisitor, payouts));
        assertStatus(ResultStatus.ACCEPT, evaluate(template, fullVisitor, payouts));
        verify(payments, times(3)).countSuccess("user_id", model, window, scope);
        verify(payouts, times(3)).countSuccess("user_id", model, window, scope);
    }

    @Test
    void preservesTimeWindowAndWalletScope() {
        var expectedWindow = TimeWindow.builder().start(48).end(24).timeUnit("hours").build();
        when(payouts.count("user_id", model, expectedWindow, List.of("party_id", "wallet_id")))
                .thenReturn(1);
        assertStatus(ResultStatus.ACCEPT, evaluate("""
                rule: countPayouts("user_id", 48, 24, hours, "party_id", "wallet_id") > 0 -> accept;
                """, false, payouts));
        verify(payouts).count("user_id", model, expectedWindow, List.of("party_id", "wallet_id"));
    }

    @Test
    void usesCatchForUnavailablePayoutHistoryWithoutFallingBackToPayments() {
        assertStatus(ResultStatus.HIGH_RISK, evaluate("""
                rule: countPayouts("user_id", 24, hours) > 0 -> accept catch: highRisk;
                """, false, null));
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @Test
    void usesCatchForHistoryErrorsAndUnknownFields() {
        when(payouts.count(eq("user_id"), eq(model), any(), any()))
                .thenThrow(new IllegalStateException("History unavailable"));
        assertStatus(ResultStatus.HIGH_RISK, evaluate("""
                rule: countPayouts("user_id", 24, hours) > 0 -> accept catch: highRisk;
                """, false, payouts));
        assertStatus(ResultStatus.HIGH_RISK, evaluate("""
                rule: countPayouts("customer_id", 24, hours) > 0 -> accept catch: highRisk;
                """, false, payouts));
    }

    private ResultModel evaluate(String template, boolean fullVisitor,
                                 CountPayoutAggregator<BaseModel, String> history) {
        var lexer = new FraudoRulesLexer(CharStreams.fromString(template));
        var parser = new FraudoRulesParser(new CommonTokenStream(lexer));
        var tree = parser.parse();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        var aggregators = history == null
                ? new AggregatorBundle<>(payments, paymentSums, paymentUnique, null, null, null)
                : new AggregatorBundle<>(payments, paymentSums, paymentUnique, history, payoutSums, payoutUnique);
        var resolvers = new ResolverBundle<>(null, fields, new GroupFieldsResolverImpl<>(fields),
                new TimeWindowResolverImpl(), null, null);
        var bundle = new VisitorBundle<>(aggregators, resolvers, new FinderBundle<BaseModel, String>(null));
        FraudVisitorFactory factory = fullVisitor ? new FullVisitorFactoryImpl() : new FraudVisitorFactoryImpl();
        try (var visitor = factory.createVisitor(bundle)) {
            return visitor.visit(tree, model);
        }
    }

    private void assertStatus(ResultStatus expected, ResultModel result) {
        assertEquals(expected, ResultUtils.findFirstNotNotifyStatus(result).orElseThrow().getResultStatus());
    }
}
