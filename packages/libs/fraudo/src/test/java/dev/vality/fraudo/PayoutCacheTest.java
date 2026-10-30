package dev.vality.fraudo;

import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.BaseModel;
import dev.vality.fraudo.model.TimeWindow;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PayoutCacheTest extends AbstractPayoutTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void ignoresFormattingWhitespaceInCacheKey(boolean fullVisitor) throws Exception {
        when(payouts.count("user_id", model, window, List.of())).thenReturn(1);
        assertStatus(ResultStatus.DECLINE, evaluate("/rules/payout/cacheWhitespace.frd", fullVisitor));
        verify(payouts).count("user_id", model, window, List.of());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void doesNotNormalizeEquivalentTimeWindowSyntax(boolean fullVisitor) throws Exception {
        when(payouts.count("user_id", model, window, List.of())).thenReturn(1);
        assertStatus(ResultStatus.DECLINE, evaluate("/rules/payout/cacheEquivalentWindows.frd", fullVisitor));
        verify(payouts, times(2)).count("user_id", model, window, List.of());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cachesEveryFunctionWithinAndBetweenRules(boolean fullVisitor) throws Exception {
        when(payouts.count("user_id", model, window, List.of())).thenReturn(1);
        when(payouts.countSuccess("user_id", model, window, List.of())).thenReturn(1);
        when(payouts.countError("user_id", model, window, List.of())).thenReturn(1);
        when(payouts.countError("user_id", model, window, "failed", List.of("party_id"))).thenReturn(1);
        when(payoutSums.sum("user_id", model, window, List.of())).thenReturn(1.0);
        when(payoutSums.sumSuccess("user_id", model, window, List.of())).thenReturn(1.0);
        when(payoutSums.sumError("user_id", model, window, List.of())).thenReturn(1.0);
        when(payoutSums.sumError("user_id", model, window, "failed", List.of("party_id"))).thenReturn(1.0);
        when(payoutUnique.countUniqueValue("user_id", model, "card_token", window, List.of())).thenReturn(1);

        var result = evaluate("/rules/payout/cacheRepeated.frd", fullVisitor);

        assertStatus(ResultStatus.ACCEPT, result);
        assertEquals(19, result.getRuleResults().size());
        assertEquals(4, mockingDetails(payouts).getInvocations().size());
        assertEquals(4, mockingDetails(payoutSums).getInvocations().size());
        assertEquals(1, mockingDetails(payoutUnique).getInvocations().size());
        verifyNoInteractions(payments, paymentSums, paymentUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void distinguishesAllCacheKeyArguments(boolean fullVisitor) throws Exception {
        when(payouts.count("user_id", model, window, List.of())).thenReturn(1);
        when(payouts.count("card_token", model, window, List.of())).thenReturn(2);
        when(payouts.count("user_id", model,
                TimeWindow.builder().start(24).timeUnit("days").build(), List.of())).thenReturn(3);
        when(payouts.count("user_id", model,
                TimeWindow.builder().start(48).end(24).timeUnit("hours").build(), List.of())).thenReturn(4);
        when(payouts.count("user_id", model,
                TimeWindow.builder().start(48).end(12).timeUnit("hours").build(), List.of())).thenReturn(23);
        when(payouts.count("user_id", model,
                TimeWindow.builder().start(48).timeUnit("hours").build(), List.of())).thenReturn(24);
        when(payouts.count("user_id", model, window, List.of("party_id"))).thenReturn(5);
        when(payouts.count("user_id", model, window, List.of("wallet_id"))).thenReturn(6);
        when(payouts.count("user_id", model, window, List.of("party_id", "currency"))).thenReturn(7);
        when(payouts.count("user_id", model, window, List.of("currency", "party_id"))).thenReturn(8);
        when(payouts.countSuccess("user_id", model, window, List.of())).thenReturn(9);
        when(payouts.countError("user_id", model, window, List.of())).thenReturn(10);
        when(payouts.countError("user_id", model, window, "error-a", List.of())).thenReturn(11);
        when(payouts.countError("user_id", model, window, "error-b", List.of())).thenReturn(12);
        when(payoutSums.sum("user_id", model, window, List.of())).thenReturn(13.0);
        when(payoutSums.sumSuccess("user_id", model, window, List.of())).thenReturn(14.0);
        when(payoutSums.sumError("user_id", model, window, List.of())).thenReturn(15.0);
        when(payoutSums.sumError("user_id", model, window, "failed", List.of())).thenReturn(16.0);
        when(payoutSums.sumError("user_id", model, window, "other", List.of())).thenReturn(22.0);
        when(payoutUnique.countUniqueValue("user_id", model, "card_token", window, List.of())).thenReturn(17);
        when(payoutUnique.countUniqueValue("card_token", model, "user_id", window, List.of())).thenReturn(18);
        when(payoutUnique.countUniqueValue("user_id", model, "currency", window, List.of())).thenReturn(19);
        when(payoutUnique.countUniqueValue("user_id", model, "card_token",
                TimeWindow.builder().start(24).timeUnit("days").build(), List.of())).thenReturn(20);
        when(payoutUnique.countUniqueValue("user_id", model, "card_token", window, List.of("party_id")))
                .thenReturn(21);

        var result = evaluate("/rules/payout/cacheKeys.frd", fullVisitor);

        assertStatus(ResultStatus.ACCEPT, result);
        assertEquals(9, result.getRuleResults().size());
        assertEquals(14, mockingDetails(payouts).getInvocations().size());
        assertEquals(5, mockingDetails(payoutSums).getInvocations().size());
        assertEquals(5, mockingDetails(payoutUnique).getInvocations().size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cachesZero(boolean fullVisitor) throws Exception {
        when(payouts.count("user_id", model, window, List.of())).thenReturn(0);
        assertStatus(ResultStatus.DECLINE, evaluate("/rules/payout/cacheZero.frd", fullVisitor));
        verify(payouts).count("user_id", model, window, List.of());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void clearsCacheWhenReusingVisitorForDifferentModels(boolean fullVisitor) throws Exception {
        var first = model(1);
        var second = model(2);
        when(payouts.count(eq("user_id"), any(), eq(window), eq(List.of())))
                .thenAnswer(call -> ((BaseModel) call.getArgument(1)).getAmount().intValue());
        var parseContext = getParseContext("/rules/payout/cacheModel.frd");
        try (var visitor = createVisitor(fullVisitor)) {
            assertStatus(ResultStatus.DECLINE, visitor.visit(parseContext, first));
            assertStatus(ResultStatus.DECLINE, visitor.visit(parseContext, second));
        }
        verify(payouts).count("user_id", first, window, List.of());
        verify(payouts).count("user_id", second, window, List.of());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void clearsCacheWhenTraversalThrows(boolean fullVisitor) throws Exception {
        var first = model(1);
        var second = model(2);
        when(payouts.count(eq("user_id"), any(), eq(window), eq(List.of())))
                .thenAnswer(call -> ((BaseModel) call.getArgument(1)).getAmount().intValue());
        var parseContext = getParseContext("/rules/payout/cacheModel.frd");
        try (var visitor = createVisitor(fullVisitor)) {
            ParseTree failingTree = mock(ParseTree.class);
            when(failingTree.accept(any())).thenAnswer(call -> {
                visitor.visit(parseContext);
                throw new IllegalStateException("Traversal interrupted");
            });
            assertThrows(IllegalStateException.class, () -> visitor.visit(failingTree, first));
            assertStatus(ResultStatus.DECLINE, visitor.visit(parseContext, second));
        }
        verify(payouts).count("user_id", first, window, List.of());
        verify(payouts).count("user_id", second, window, List.of());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void isolatesThreadsSharingVisitorAndParseTree(boolean fullVisitor) throws Exception {
        var first = model(1);
        var second = model(2);
        CyclicBarrier bothCallsStarted = new CyclicBarrier(2);
        when(payouts.count(eq("user_id"), any(), eq(window), eq(List.of()))).thenAnswer(call -> {
            BaseModel current = call.getArgument(1);
            bothCallsStarted.await(5, TimeUnit.SECONDS);
            return current.getAmount().intValue();
        });
        var parseContext = getParseContext("/rules/payout/cacheModel.frd");
        var visitor = createVisitor(fullVisitor);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var firstResult = executor.submit(() -> {
                try {
                    return visitor.visit(parseContext, first);
                } finally {
                    visitor.close();
                }
            });
            var secondResult = executor.submit(() -> {
                try {
                    return visitor.visit(parseContext, second);
                } finally {
                    visitor.close();
                }
            });
            assertStatus(ResultStatus.DECLINE, firstResult.get(5, TimeUnit.SECONDS));
            assertStatus(ResultStatus.DECLINE, secondResult.get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
        verify(payouts).count("user_id", first, window, List.of());
        verify(payouts).count("user_id", second, window, List.of());
    }

    private BaseModel model(int amount) {
        var value = new BaseModel();
        value.setUserId("test-user-" + amount);
        value.setAmount((long) amount);
        return value;
    }
}
