package dev.vality.fraudbusters.fraud.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCodeFormatValidatorTest {

    private final ErrorCodeFormatValidator validator = new ErrorCodeFormatValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "rule: a: countError(\"email\", 1, days, \"authorization_failed: expired\") > 3 -> decline;",
            "rule: a: countError(\"email\", 1, days, \"authorization_failed :expired\") > 3 -> decline;",
            "rule: a: countError(\"email\", 1, days, \" insufficient_funds\", \"party_id\") > 3 -> decline;",
            "rule: a: sumError(\"email\", 1, days, \"insufficient_funds \") > 3 -> decline;",
            "rule: a: countPayoutsError(\"email\", 1, days, \"limit: exceeded\") > 3 -> decline;",
            "rule: a: sumPayoutsError(\"email\", 1, days, \"limit: exceeded\") > 3 -> decline;"
    })
    void findsErrorCodeWithMisplacedSpace(String template) {
        List<String> errors = validator.findMisformattedErrorCodes(template);

        assertEquals(1, errors.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "rule: a: countError(\"email\", 1, days, \"authorization_failed:expired\") > 3 -> decline;",
            "rule: a: countError(\"email\", 1, days, \"unknown error\") > 3 -> decline;",
            "rule: a: countError(\"email\", 1, days) > 3 -> decline;",
            "rule: a: countError(\"email\", 1, days, \"insufficient_funds\", \"party_id\", \"shop_id\") > 3 -> 3ds;",
            "rule: a: in(\"email\", \"a: b\") and countSuccess(\"email\", 1, days) > 3 -> decline;"
    })
    void acceptsErrorCodeThatHistoryCanStore(String template) {
        assertTrue(validator.findMisformattedErrorCodes(template).isEmpty());
    }

    @Test
    void reportsEveryMisformattedCodeWithItsLine() {
        String template = """
                rule: a: countError("email", 1, days, "authorization_failed: expired") > 3 -> decline;
                rule: b: countError("email", 1, days, "authorization_failed:expired") > 3
                    and sumError("email", 1, days, "card_expired ") > 100 -> decline;
                """;

        List<String> errors = validator.findMisformattedErrorCodes(template);

        assertEquals(2, errors.size());
        assertTrue(errors.get(0).startsWith("line 1:"));
        assertTrue(errors.get(1).startsWith("line 3:"));
    }

}
