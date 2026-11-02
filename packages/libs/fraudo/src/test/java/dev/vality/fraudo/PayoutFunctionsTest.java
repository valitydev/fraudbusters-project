package dev.vality.fraudo;

import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.TimeWindow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayoutFunctionsTest extends AbstractPayoutTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void routesEveryPayoutFunctionToPayoutHistory(boolean fullVisitor) throws Exception {
        var scope = List.of("party_id", "currency");
        when(payouts.count("user_id", model, window, scope)).thenReturn(2);
        when(payouts.countSuccess("user_id", model, window, scope)).thenReturn(1);
        when(payouts.countError("user_id", model, window, "failed", scope)).thenReturn(1);
        when(payoutSums.sum("user_id", model, window, scope)).thenReturn(200.0);
        when(payoutSums.sumSuccess("user_id", model, window, scope)).thenReturn(100.0);
        when(payoutSums.sumError("user_id", model, window, "failed", scope)).thenReturn(100.0);
        when(payoutUnique.countUniqueValue("user_id", model, "card_token", window, scope)).thenReturn(2);

        var result = evaluate("/rules/payout/allFunctions.frd", fullVisitor);

        assertStatus(ResultStatus.DECLINE, result);
        verify(payoutUnique).countUniqueValue("user_id", model, "card_token", window, scope);
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void preservesFirstMatchAndFullTraversalWithPayoutFunctions(boolean fullVisitor) throws Exception {
        when(payouts.count("user_id", model, window, List.of())).thenReturn(2);

        var result = evaluate("/rules/payout/traversal.frd", fullVisitor);

        assertEquals(fullVisitor ? 2 : 1, result.getRuleResults().size());
        assertEquals(ResultStatus.DECLINE, result.getRuleResults().get(0).getResultStatus());
        if (fullVisitor) {
            assertEquals(ResultStatus.HIGH_RISK, result.getRuleResults().get(1).getResultStatus());
        }
        verify(payouts).count("user_id", model, window, List.of());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void allowsThreeDsForPaymentRuleUsingPayoutHistory(boolean fullVisitor) throws Exception {
        when(payments.countSuccess("user_id", model, window, List.of())).thenReturn(0);
        when(payouts.count("user_id", model, window, List.of())).thenReturn(2);

        assertStatus(ResultStatus.THREE_DS, evaluate("/rules/payout/paymentThreeDs.frd", fullVisitor));

        verify(payments).countSuccess("user_id", model, window, List.of());
        verify(payouts).count("user_id", model, window, List.of());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void allowsThreeDsCatchForPaymentRuleWhenPayoutHistoryFails(boolean fullVisitor) throws Exception {
        when(payments.countSuccess("user_id", model, window, List.of())).thenReturn(0);
        when(payouts.count("user_id", model, window, List.of()))
                .thenThrow(new IllegalStateException("History unavailable"));

        assertStatus(ResultStatus.THREE_DS, evaluate("/rules/payout/paymentThreeDsCatch.frd", fullVisitor));

        verify(payments).countSuccess("user_id", model, window, List.of());
        verify(payouts).count("user_id", model, window, List.of());
    }

    @Test
    void supportsErrorFunctionsWithoutCode() throws Exception {
        when(payouts.countError("user_id", model, window, List.of())).thenReturn(1);
        when(payoutSums.sumError("user_id", model, window, List.of())).thenReturn(100.0);
        assertStatus(ResultStatus.DECLINE, evaluate("/rules/payout/errorsNoCode.frd", false));
    }

    @Test
    void separatesPaymentAndPayoutFunctionsAndCachesRepeatedPayoutCalls() throws Exception {
        when(payments.countSuccess("user_id", model, window, List.of("party_id"))).thenReturn(0);
        when(payouts.count("user_id", model, window, List.of("party_id"))).thenReturn(2);
        assertStatus(ResultStatus.DECLINE, evaluate("/rules/payout/mixedCache.frd", false));
        verify(payouts).count("user_id", model, window, List.of("party_id"));
        verify(payments).countSuccess("user_id", model, window, List.of("party_id"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void combinesSuccessfulPaymentsAndPayoutsWithAnd(boolean fullVisitor) throws Exception {
        var paymentWindow = TimeWindow.builder().start(30).timeUnit("days").build();
        var scope = List.of("party_id");
        when(payments.countSuccess("user_id", model, paymentWindow, scope)).thenReturn(0, 1, 0);
        when(payouts.countSuccess("user_id", model, window, scope)).thenReturn(4, 3);
        String template = "/rules/payout/mixedAnd.frd";

        assertStatus(ResultStatus.DECLINE, evaluate(template, fullVisitor));
        assertStatus(ResultStatus.ACCEPT, evaluate(template, fullVisitor));
        assertStatus(ResultStatus.ACCEPT, evaluate(template, fullVisitor));
        verify(payments, times(3)).countSuccess("user_id", model, paymentWindow, scope);
        verify(payouts, times(2)).countSuccess("user_id", model, window, scope);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void combinesSuccessfulPaymentsAndPayoutsWithOrNotAndParentheses(boolean fullVisitor) throws Exception {
        var scope = List.of("party_id");
        when(payments.countSuccess("user_id", model, window, scope)).thenReturn(0, 0, 1);
        when(payouts.countSuccess("user_id", model, window, scope)).thenReturn(2, 4);
        String template = "/rules/payout/mixedOrNot.frd";

        assertStatus(ResultStatus.DECLINE, evaluate(template, fullVisitor));
        assertStatus(ResultStatus.ACCEPT, evaluate(template, fullVisitor));
        assertStatus(ResultStatus.ACCEPT, evaluate(template, fullVisitor));
        verify(payments, times(3)).countSuccess("user_id", model, window, scope);
        verify(payouts, times(2)).countSuccess("user_id", model, window, scope);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void comparesSuccessfulPayoutCountDirectlyToSuccessfulPaymentCount(boolean fullVisitor) throws Exception {
        var scope = List.of("party_id");
        when(payouts.countSuccess("user_id", model, window, scope)).thenReturn(4, 1, 2);
        when(payments.countSuccess("user_id", model, window, scope)).thenReturn(3, 2, 2);
        String template = "/rules/payout/compareCounts.frd";

        assertStatus(ResultStatus.HIGH_RISK, evaluate(template, fullVisitor));
        assertStatus(ResultStatus.ACCEPT, evaluate(template, fullVisitor));
        assertStatus(ResultStatus.ACCEPT, evaluate(template, fullVisitor));
        verify(payments, times(3)).countSuccess("user_id", model, window, scope);
        verify(payouts, times(3)).countSuccess("user_id", model, window, scope);
    }

    @Test
    void preservesTimeWindowAndWalletScope() throws Exception {
        var expectedWindow = TimeWindow.builder().start(48).end(24).timeUnit("hours").build();
        when(payouts.count("user_id", model, expectedWindow, List.of("party_id", "wallet_id")))
                .thenReturn(1);
        assertStatus(ResultStatus.ACCEPT, evaluate("/rules/payout/windowWallet.frd", false));
        verify(payouts).count("user_id", model, expectedWindow, List.of("party_id", "wallet_id"));
    }

    @Test
    void usesCatchForUnavailablePayoutHistoryWithoutFallingBackToPayments() throws Exception {
        payouts = null;
        payoutSums = null;
        payoutUnique = null;
        assertStatus(ResultStatus.HIGH_RISK, evaluate("/rules/payout/unavailableHistory.frd", false));
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @Test
    void usesCatchForHistoryErrorsAndUnknownFields() throws Exception {
        when(payouts.count(eq("user_id"), eq(model), any(), any()))
                .thenThrow(new IllegalStateException("History unavailable"));
        assertStatus(ResultStatus.HIGH_RISK, evaluate("/rules/payout/historyFailure.frd", false));
        assertStatus(ResultStatus.HIGH_RISK, evaluate("/rules/payout/unknownField.frd", false));
    }

}
