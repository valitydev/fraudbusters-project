package dev.vality.fraudo;

import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.TimeWindow;
import dev.vality.fraudo.utils.ResultUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

public class UniquePayoutTest extends AbstractPayoutTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unknownUniqueFieldTest(boolean fullVisitor) throws Exception {
        assertStatus(ResultStatus.HIGH_RISK, evaluate("/rules/payout/unknownUniqueField.frd", fullVisitor));
        verifyNoInteractions(payoutUnique, payments, paymentUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void uniquePayoutsTest(boolean fullVisitor) throws Exception {
        List<String> scope = List.of("party_id", "currency");
        when(payoutUnique.countUniqueValue("user_id", model, "card_token", window, scope)).thenReturn(2);
        var parseContext = getParseContext("/rules/payout/uniquePayouts.frd");
        try (var visitor = createVisitor(fullVisitor)) {
            assertStatus(ResultStatus.DECLINE, visitor.visit(parseContext, model));
            when(payoutUnique.countUniqueValue("user_id", model, "card_token", window, scope)).thenReturn(0);
            assertFalse(ResultUtils.findFirstNotNotifyStatus(visitor.visit(parseContext, model)).isPresent());
        }
        verify(payoutUnique, times(2)).countUniqueValue("user_id", model, "card_token", window, scope);
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

}
