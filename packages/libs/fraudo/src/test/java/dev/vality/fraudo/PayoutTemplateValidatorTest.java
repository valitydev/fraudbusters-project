package dev.vality.fraudo;

import dev.vality.fraudo.payout.validator.PayoutTemplateValidator;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayoutTemplateValidatorTest {

    private final PayoutTemplateValidator validator = new PayoutTemplateValidator();

    @ParameterizedTest
    @ValueSource(strings = {"trust", "accept", "highRisk", "decline", "notify",
            "declineAndNotify", "acceptAndNotify"})
    void acceptsPayoutResults(String result) {
        assertTrue(validator.validate("rule: amount() > 0 -> " + result + ";").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "rule: amount() > 0 -> 3ds;",
            "rule: amount() > 0 -> accept catch: 3ds;",
            "rule: isTrusted() -> accept;",
            "rule: isTrusted(\"template\") -> accept;",
            "rule: amount() > 0 OR isTrusted() -> accept;",
            "rule: isMobile() -> accept;",
            "rule: isRecurrent() -> accept;"})
    void rejectsUnsupportedPayoutFeaturesWithoutChangingPaymentSyntax(String template) {
        var parser = new FraudoRulesParser(new CommonTokenStream(
                new FraudoRulesLexer(CharStreams.fromString(template))));
        parser.parse();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        assertFalse(validator.validate(template).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"3ds;", "accept catch: 3ds;"})
    void rejectsThreeDsInMixedHistoryRulesForPayouts(String result) {
        String template = """
                rule: countSuccess("user_id", 24, hours) = 0
                  AND countPayouts("user_id", 24, hours) > 1 ->
                """ + result;

        assertEquals(List.of("3ds is not supported for payouts"), validator.validate(template));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "rule: amount() > 0 -> accept; @",
            "rule: amount() > -> accept;",
            "rule: countPayoutsPending(\"user_id\", 24, hours) > 0 -> accept;",
            "rule: customerId() = \"customer\" -> accept;"})
    void rejectsLexicalAndSyntaxErrors(String template) {
        assertFalse(validator.validate(template).isEmpty());
    }

    @Test
    void acceptsCrossHistoryAndListRules() {
        assertTrue(validator.validate("""
                rule: countSuccess("user_id", 30, days, "party_id") = 0 -> decline;
                rule: sumPayouts("user_id", 24, hours, "party_id", "currency") > 100000 -> highRisk;
                rule: inBlackList("user_id") -> decline;
                rule: userId() != "UNKNOWN" -> notify;
                """).isEmpty());
    }

    @Test
    void validatesWholeTemplateAndIgnoresFunctionNamesInsideStrings() {
        assertEquals(2, validator.validate("""
                rule: amount() > 0 -> 3ds;
                rule: amount() > 0 -> accept catch: 3ds;
                """).size());
        assertTrue(validator.validate("rule: like(\"email\", \"isTrusted 3ds\") -> accept;").isEmpty());
    }
}
