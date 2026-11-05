package dev.vality.fraudo;

import dev.vality.fraudo.constant.ResultStatus;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verifyNoInteractions;

class PaymentCacheTest extends AbstractPayoutTest {

    private static final List<String> FUNCTIONS = List.of(
            "count(\"user_id\", %s)",
            "countSuccess(\"user_id\", %s)",
            "countPending(\"user_id\", %s)",
            "countError(\"user_id\", %s)",
            "countError(\"user_id\", %s, \"failed\", \"party_id\")",
            "countChargeback(\"user_id\", %s)",
            "countRefund(\"user_id\", %s)",
            "sum(\"user_id\", %s)",
            "sumSuccess(\"user_id\", %s)",
            "sumError(\"user_id\", %s)",
            "sumError(\"user_id\", %s, \"failed\", \"party_id\")",
            "sumChargeback(\"user_id\", %s)",
            "sumRefund(\"user_id\", %s)",
            "unique(\"user_id\", \"card_token\", %s)");

    @ParameterizedTest
    @MethodSource("equivalentQueries")
    void normalizesEquivalentWindows(boolean fullVisitor, String function) {
        String expression = Stream.of("24", "24, hours", "24, 0, hours")
                .map(value -> function.formatted(value) + " >= 0")
                .collect(Collectors.joining(" AND "));

        evaluateExpression(expression, fullVisitor);

        var calls = Stream.of(payments, paymentSums, paymentUnique)
                .flatMap(aggregator -> mockingDetails(aggregator).getInvocations().stream())
                .toList();
        assertEquals(1, calls.size());
        int windowArgument = function.startsWith("unique(") ? 3 : 2;
        assertEquals(window, calls.get(0).getArgument(windowArgument));
        verifyNoInteractions(payouts, payoutSums, payoutUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void distinguishesHistoryFunctions(boolean fullVisitor) {
        String expression = FUNCTIONS.stream()
                .map(function -> function.formatted("24") + " >= 0")
                .collect(Collectors.joining(" AND "));

        evaluateExpression(expression, fullVisitor);

        assertEquals(7, mockingDetails(payments).getInvocations().size());
        assertEquals(6, mockingDetails(paymentSums).getInvocations().size());
        assertEquals(1, mockingDetails(paymentUnique).getInvocations().size());
        verifyNoInteractions(payouts, payoutSums, payoutUnique);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void separatesPaymentAndPayoutHistory(boolean fullVisitor) {
        evaluateExpression("""
                count("user_id", 24) >= 0 AND countPayouts("user_id", 24) >= 0
                AND sum("user_id", 24) >= 0 AND sumPayouts("user_id", 24) >= 0
                AND unique("user_id", "card_token", 24) >= 0
                AND uniquePayouts("user_id", "card_token", 24) >= 0
                """, fullVisitor);

        Stream.of(payments, paymentSums, paymentUnique, payouts, payoutSums, payoutUnique)
                .forEach(aggregator -> assertEquals(1, mockingDetails(aggregator).getInvocations().size()));
    }

    @ParameterizedTest
    @MethodSource("differentQueries")
    void distinguishesQueryArguments(boolean fullVisitor, String first, String second) {
        evaluateExpression(first + " >= 0 AND " + second + " >= 0", fullVisitor);

        long calls = Stream.of(payments, paymentSums, paymentUnique)
                .mapToLong(aggregator -> mockingDetails(aggregator).getInvocations().size()).sum();
        assertEquals(2, calls);
    }

    private void evaluateExpression(String expression, boolean fullVisitor) {
        var lexer = new FraudoRulesLexer(CharStreams.fromString("rule: " + expression + " -> decline;"));
        var parser = new FraudoRulesParser(new CommonTokenStream(lexer));
        var tree = parser.parse();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        try (var visitor = createVisitor(fullVisitor)) {
            assertStatus(ResultStatus.DECLINE, visitor.visit(tree, model));
        }
    }

    private static Stream<Arguments> equivalentQueries() {
        return Stream.of(false, true)
                .flatMap(full -> FUNCTIONS.stream()
                        .map(function -> Arguments.of(full, function)));
    }

    private static Stream<Arguments> differentQueries() {
        List<List<String>> queries = List.of(
                List.of("count(\"user_id\", 24)", "count(\"card_token\", 24)"),
                List.of("count(\"user_id\", 24)", "count(\"user_id\", 48)"),
                List.of("count(\"user_id\", 24)", "count(\"user_id\", 24, days)"),
                List.of("count(\"user_id\", 48, 24, hours)", "count(\"user_id\", 48, 12, hours)"),
                List.of("count(\"user_id\", 24)", "count(\"user_id\", 24, hours, \"party_id\")"),
                List.of("count(\"user_id\", 24, hours, \"party_id\")",
                        "count(\"user_id\", 24, hours, \"wallet_id\")"),
                List.of("count(\"user_id\", 24, hours, \"party_id\", \"currency\")",
                        "count(\"user_id\", 24, hours, \"currency\", \"party_id\")"),
                List.of("countError(\"user_id\", 24)", "countError(\"user_id\", 24, \"failed\")"),
                List.of("countError(\"user_id\", 24, \"failed\")", "countError(\"user_id\", 24, \"other\")"),
                List.of("sumError(\"user_id\", 24)", "sumError(\"user_id\", 24, \"failed\")"),
                List.of("sumError(\"user_id\", 24, \"failed\")", "sumError(\"user_id\", 24, \"other\")"),
                List.of("unique(\"user_id\", \"card_token\", 24)", "unique(\"card_token\", \"user_id\", 24)"),
                List.of("unique(\"user_id\", \"card_token\", 24)", "unique(\"user_id\", \"currency\", 24)"));
        return Stream.of(false, true)
                .flatMap(full -> queries.stream()
                        .map(pair -> Arguments.of(full, pair.get(0), pair.get(1))));
    }
}
