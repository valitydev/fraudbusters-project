package dev.vality.fraudo;

import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.exception.FieldUnsetException;
import dev.vality.fraudo.aggregator.UniqueValueAggregator;
import dev.vality.fraudo.payout.aggregator.CountPayoutAggregator;
import dev.vality.fraudo.payout.aggregator.SumPayoutAggregator;
import dev.vality.fraudo.converter.TrustConditionConverter;
import dev.vality.fraudo.model.BaseModel;
import dev.vality.fraudo.payment.visitor.impl.FirstFindVisitorImpl;
import dev.vality.fraudo.visitor.FirstFindRulesVisitor;
import dev.vality.fraudo.visitor.FullRulesVisitor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PayoutFailureTest extends AbstractPayoutTest {

    @ParameterizedTest
    @MethodSource("functions")
    void missingFunctionVisitorTest(String function, boolean fullVisitor) throws Exception {
        var parseContext = getParseContext("/rules/payout/failure/" + function + ".frd");
        try (FirstFindVisitorImpl<BaseModel, String> visitor = fullVisitor
                ? new FullRulesVisitor<>(null, null, null, null, null, fields, new TrustConditionConverter(), null)
                : new FirstFindRulesVisitor<>(null, null, null, null, null, fields,
                        new TrustConditionConverter(), null)) {
            assertStatus(ResultStatus.HIGH_RISK, visitor.visit(parseContext, model));
        }
        verifyNoInteractions(payouts, payoutSums, payoutUnique, payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void keepsExistingNotifyFallbackWithoutCatch(boolean fullVisitor) throws Exception {
        when(payouts.count("user_id", model, window, List.of()))
                .thenThrow(new IllegalStateException("History unavailable"));
        var result = evaluate("/rules/payout/noCatch.frd", fullVisitor);
        assertEquals(1, result.getRuleResults().size());
        assertEquals(ResultStatus.NOTIFY, result.getRuleResults().get(0).getResultStatus());
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @MethodSource("functions")
    void unrelatedAggregatorsAreNotRequired(String function, boolean fullVisitor) throws Exception {
        if (function.startsWith("count")) {
            payoutSums = null;
            payoutUnique = null;
        } else if (function.startsWith("sum")) {
            payouts = null;
            payoutUnique = null;
        } else {
            payouts = null;
            payoutSums = null;
        }
        var result = evaluate("/rules/payout/failure/" + function + ".frd", fullVisitor);
        // Mockito's default numeric zero is a valid non-matching history result, not an error.
        assertEquals(0, result.getRuleResults().size());
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    static Stream<Arguments> functions() {
        return Stream.of("count", "countSuccess", "countError", "countErrorCode", "sum", "sumSuccess",
                        "sumError", "sumErrorCode", "unique")
                .flatMap(name -> Stream.of(Arguments.of(name, false), Arguments.of(name, true)));
    }

    @ParameterizedTest
    @MethodSource("functions")
    void historyErrorTest(String function, boolean fullVisitor) throws Exception {
        replacePayoutAggregators(invocation -> {
            throw new IllegalStateException("History unavailable");
        });
        assertStatus(ResultStatus.HIGH_RISK, evaluate("/rules/payout/failure/" + function + ".frd", fullVisitor));
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @MethodSource("functions")
    void missingAggregatorTest(String function, boolean fullVisitor) throws Exception {
        final var originalCount = payouts;
        final var originalSum = payoutSums;
        final var originalUnique = payoutUnique;
        payouts = null;
        payoutSums = null;
        payoutUnique = null;
        assertStatus(ResultStatus.HIGH_RISK,
                evaluate("/rules/payout/failure/" + function + ".frd", fullVisitor));
        verifyNoInteractions(originalCount, originalSum, originalUnique, payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @MethodSource("functions")
    void missingHistoryFieldTest(String function, boolean fullVisitor) throws Exception {
        replacePayoutAggregators(invocation -> {
            throw new FieldUnsetException();
        });
        assertStatus(ResultStatus.HIGH_RISK, evaluate("/rules/payout/failure/" + function + ".frd", fullVisitor));
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @MethodSource("functions")
    void nullHistoryResultTest(String function, boolean fullVisitor) throws Exception {
        replacePayoutAggregators(invocation -> null);
        assertStatus(ResultStatus.HIGH_RISK, evaluate("/rules/payout/failure/" + function + ".frd", fullVisitor));
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    static Stream<Arguments> retries() {
        return functions().flatMap(arguments -> Stream.of(
                Arguments.of(arguments.get()[0], arguments.get()[1], false),
                Arguments.of(arguments.get()[0], arguments.get()[1], true)));
    }

    @ParameterizedTest
    @MethodSource("retries")
    void retriesFailedFunctionInsteadOfCachingCatchResult(
            String function, boolean fullVisitor, boolean nullResult) throws Exception {
        AtomicInteger calls = new AtomicInteger();
        replacePayoutAggregators(invocation -> {
            if (calls.incrementAndGet() == 1) {
                if (nullResult) {
                    return null;
                }
                throw new IllegalStateException("History unavailable");
            }
            if (invocation.getMethod().getReturnType() == Double.class) {
                return 1.0;
            }
            return 1;
        });
        var result = evaluate("/rules/payout/retry/" + function + ".frd", fullVisitor);
        assertStatus(ResultStatus.DECLINE, result);
        assertEquals(ResultStatus.NOTIFY, result.getRuleResults().get(0).getResultStatus());
        assertEquals(2, result.getRuleResults().size());
        assertEquals(2, calls.get());
    }

    @SuppressWarnings("unchecked")
    private void replacePayoutAggregators(Answer<?> answer) {
        payouts = mock(CountPayoutAggregator.class, answer);
        payoutSums = mock(SumPayoutAggregator.class, answer);
        payoutUnique = mock(UniqueValueAggregator.class, answer);
    }
}
