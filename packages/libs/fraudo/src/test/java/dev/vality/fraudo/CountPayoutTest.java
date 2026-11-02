package dev.vality.fraudo;

import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.TimeWindow;
import dev.vality.fraudo.utils.ResultUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

public class CountPayoutTest extends AbstractPayoutTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unknownGroupingTest(boolean fullVisitor) throws Exception {
        assertStatus(ResultStatus.HIGH_RISK, evaluate("/rules/payout/unknownGrouping.frd", fullVisitor));
        verifyNoInteractions(payouts, payments, payoutSums, payoutUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void interpretsFirstStringAfterErrorWindowAsErrorCode(boolean fullVisitor) throws Exception {
        when(payouts.countError("user_id", model, window, "party_id", List.of())).thenReturn(1);
        assertStatus(ResultStatus.DECLINE, evaluate("/rules/payout/errorCodeVsGroup.frd", fullVisitor));
        verify(payouts).countError("user_id", model, window, "party_id", List.of());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void countPayoutsTest(boolean fullVisitor) throws Exception {
        List<String> scope = List.of();
        var expectedWindow = window;
        when(payouts.count("user_id", model, expectedWindow, scope)).thenReturn(2);
        var parseContext = getParseContext("/rules/payout/countPayouts.frd");
        try (var visitor = createVisitor(fullVisitor)) {
            assertStatus(ResultStatus.DECLINE, visitor.visit(parseContext, model));
            when(payouts.count("user_id", model, expectedWindow, scope)).thenReturn(0);
            assertFalse(ResultUtils.findFirstNotNotifyStatus(visitor.visit(parseContext, model)).isPresent());
        }
        verify(payouts, times(2)).count("user_id", model, expectedWindow, scope);
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void countPayoutsSuccessTest(boolean fullVisitor) throws Exception {
        List<String> scope = List.of("party_id", "currency");
        var expectedWindow = TimeWindow.builder().start(30).timeUnit("calendar_days").build();
        when(payouts.countSuccess("user_id", model, expectedWindow, scope)).thenReturn(2);
        var parseContext = getParseContext("/rules/payout/countPayoutsSuccess.frd");
        try (var visitor = createVisitor(fullVisitor)) {
            assertStatus(ResultStatus.DECLINE, visitor.visit(parseContext, model));
            when(payouts.countSuccess("user_id", model, expectedWindow, scope)).thenReturn(0);
            assertFalse(ResultUtils.findFirstNotNotifyStatus(visitor.visit(parseContext, model)).isPresent());
        }
        verify(payouts, times(2)).countSuccess("user_id", model, expectedWindow, scope);
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void countPayoutsErrorTest(boolean fullVisitor) throws Exception {
        List<String> scope = List.of();
        var expectedWindow = window;
        when(payouts.countError("user_id", model, expectedWindow, scope)).thenReturn(2);
        var parseContext = getParseContext("/rules/payout/countPayoutsError.frd");
        try (var visitor = createVisitor(fullVisitor)) {
            assertStatus(ResultStatus.DECLINE, visitor.visit(parseContext, model));
            when(payouts.countError("user_id", model, expectedWindow, scope)).thenReturn(0);
            assertFalse(ResultUtils.findFirstNotNotifyStatus(visitor.visit(parseContext, model)).isPresent());
        }
        verify(payouts, times(2)).countError("user_id", model, expectedWindow, scope);
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void countPayoutsErrorCodeTest(boolean fullVisitor) throws Exception {
        List<String> scope = List.of("party_id", "currency");
        var expectedWindow = TimeWindow.builder().start(48).end(24).timeUnit("hours").build();
        when(payouts.countError("user_id", model, expectedWindow, "failed", scope)).thenReturn(2);
        var parseContext = getParseContext("/rules/payout/countPayoutsErrorCode.frd");
        try (var visitor = createVisitor(fullVisitor)) {
            assertStatus(ResultStatus.DECLINE, visitor.visit(parseContext, model));
            when(payouts.countError("user_id", model, expectedWindow, "failed", scope)).thenReturn(0);
            assertFalse(ResultUtils.findFirstNotNotifyStatus(visitor.visit(parseContext, model)).isPresent());
        }
        verify(payouts, times(2)).countError("user_id", model, expectedWindow, "failed", scope);
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

}
