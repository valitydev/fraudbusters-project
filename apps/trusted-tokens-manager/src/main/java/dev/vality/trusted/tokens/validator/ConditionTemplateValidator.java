package dev.vality.trusted.tokens.validator;

import dev.vality.trusted.tokens.ConditionTemplate;
import dev.vality.trusted.tokens.InvalidRequest;
import dev.vality.trusted.tokens.validator.conditions.PaymentsConditionsValidator;
import dev.vality.trusted.tokens.validator.conditions.PayoutConditionsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static dev.vality.trusted.tokens.constants.ExceptionErrors.INVALID_REQUEST_CONDITIONS;

@Component
@RequiredArgsConstructor
public class ConditionTemplateValidator {

    private final PaymentsConditionsValidator paymentsConditionsValidator;
    private final PayoutConditionsValidator payoutConditionsValidator;

    public void validate(ConditionTemplate conditionTemplate) throws InvalidRequest {

        if (!conditionTemplate.isSetPaymentsConditions() && !conditionTemplate.isSetPayoutsConditions()) {
            throw new InvalidRequest(INVALID_REQUEST_CONDITIONS);
        }
        if (conditionTemplate.isSetPaymentsConditions()) {
            paymentsConditionsValidator.validateConditions(
                    conditionTemplate.getPaymentsConditions().getConditions());
        }
        if (conditionTemplate.isSetPayoutsConditions()) {
            payoutConditionsValidator.validateConditions(
                    conditionTemplate.getPayoutsConditions().getConditions());
        }
    }
}
