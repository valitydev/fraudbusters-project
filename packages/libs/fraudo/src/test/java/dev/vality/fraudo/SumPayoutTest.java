package dev.vality.fraudo;

import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.TimeWindow;
import dev.vality.fraudo.utils.ResultUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

public class SumPayoutTest extends AbstractPayoutTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void sumPayoutsTest(boolean fullVisitor) throws Exception {
        List<String> scope = List.of();
        var expectedWindow = window;
        when(payoutSums.sum("user_id", model, expectedWindow, scope)).thenReturn(100.25);
        var parseContext = getParseContext("/rules/payout/sumPayouts.frd");
        try (var visitor = createVisitor(fullVisitor)) {
            assertStatus(ResultStatus.DECLINE, visitor.visit(parseContext, model));
            when(payoutSums.sum("user_id", model, expectedWindow, scope)).thenReturn(0.0);
            assertFalse(ResultUtils.findFirstNotNotifyStatus(visitor.visit(parseContext, model)).isPresent());
        }
        verify(payoutSums, times(2)).sum("user_id", model, expectedWindow, scope);
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void sumPayoutsSuccessTest(boolean fullVisitor) throws Exception {
        List<String> scope = List.of("party_id", "currency");
        var expectedWindow = TimeWindow.builder().start(30).timeUnit("calendar_days").build();
        when(payoutSums.sumSuccess("user_id", model, expectedWindow, scope)).thenReturn(100.25);
        var parseContext = getParseContext("/rules/payout/sumPayoutsSuccess.frd");
        try (var visitor = createVisitor(fullVisitor)) {
            assertStatus(ResultStatus.DECLINE, visitor.visit(parseContext, model));
            when(payoutSums.sumSuccess("user_id", model, expectedWindow, scope)).thenReturn(0.0);
            assertFalse(ResultUtils.findFirstNotNotifyStatus(visitor.visit(parseContext, model)).isPresent());
        }
        verify(payoutSums, times(2)).sumSuccess("user_id", model, expectedWindow, scope);
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void sumPayoutsErrorTest(boolean fullVisitor) throws Exception {
        List<String> scope = List.of();
        var expectedWindow = window;
        when(payoutSums.sumError("user_id", model, expectedWindow, scope)).thenReturn(100.25);
        var parseContext = getParseContext("/rules/payout/sumPayoutsError.frd");
        try (var visitor = createVisitor(fullVisitor)) {
            assertStatus(ResultStatus.DECLINE, visitor.visit(parseContext, model));
            when(payoutSums.sumError("user_id", model, expectedWindow, scope)).thenReturn(0.0);
            assertFalse(ResultUtils.findFirstNotNotifyStatus(visitor.visit(parseContext, model)).isPresent());
        }
        verify(payoutSums, times(2)).sumError("user_id", model, expectedWindow, scope);
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void sumPayoutsErrorCodeTest(boolean fullVisitor) throws Exception {
        List<String> scope = List.of("party_id", "currency");
        var expectedWindow = TimeWindow.builder().start(48).end(24).timeUnit("hours").build();
        when(payoutSums.sumError("user_id", model, expectedWindow, "failed", scope)).thenReturn(100.25);
        var parseContext = getParseContext("/rules/payout/sumPayoutsErrorCode.frd");
        try (var visitor = createVisitor(fullVisitor)) {
            assertStatus(ResultStatus.DECLINE, visitor.visit(parseContext, model));
            when(payoutSums.sumError("user_id", model, expectedWindow, "failed", scope)).thenReturn(0.0);
            assertFalse(ResultUtils.findFirstNotNotifyStatus(visitor.visit(parseContext, model)).isPresent());
        }
        verify(payoutSums, times(2)).sumError("user_id", model, expectedWindow, "failed", scope);
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

}
