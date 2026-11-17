package dev.vality.fraudbusters.fraud.payment.validator;

import dev.vality.fraudbusters.fraud.payment.resolver.DatabasePaymentFieldResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentHistoryFieldValidatorTest {

    private final PaymentHistoryFieldValidator validator =
            new PaymentHistoryFieldValidator(new DatabasePaymentFieldResolver());

    @ParameterizedTest
    @ValueSource(strings = {
            "rule: a: count(\"masked_pan\", 1, days) > 3 -> decline;",
            "rule: a: countSuccess(\"masked_pan\", 1, days) > 3 -> decline;",
            "rule: a: countError(\"masked_pan\", 1, days, \"card_expired\") > 3 -> decline;",
            "rule: a: sum(\"masked_pan\", 1, days) > 300 -> decline;",
            "rule: a: unique(\"email\", \"masked_pan\", 1, days) > 3 -> decline;",
            "rule: a: count(\"card_token\", 1, days, \"masked_pan\") > 3 -> decline;",
            "rule: a: count(\"no_such_field\", 1, days) > 3 -> decline;"
    })
    void findsHistoryFunctionOverFieldNotStoredInHistory(String template) {
        assertEquals(1, validator.findFieldsMissingInHistory(template).size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "rule: a: count(\"card_token\", 1, days, \"party_id\", \"shop_id\") > 3 -> decline;",
            "rule: a: unique(\"email\", \"card_token\", 1440) > 3 -> decline;",
            "rule: a: countError(\"email\", 1, days, \"masked_pan\") > 3 -> decline;",
            "rule: a: sum(\"country_bank\", 1, days) > 300 -> decline;",
            "rule: a: in(\"masked_pan\", \"4242\") -> decline;"
    })
    void acceptsHistoryFunctionsOverStoredFields(String template) {
        assertTrue(validator.findFieldsMissingInHistory(template).isEmpty());
    }

    @Test
    void reportsFieldWithItsLine() {
        List<String> errors = validator.findFieldsMissingInHistory("""
                rule: a: count("email", 1, days) > 3 -> decline;
                rule: b: count("masked_pan", 1, days) > 3 -> decline;
                """);

        assertEquals(List.of("line 2: field masked_pan is not stored in payment history, count cannot use it"),
                errors);
    }

}
