package dev.vality.trusted.tokens.validator;

import dev.vality.trusted.tokens.ConditionTemplate;
import dev.vality.trusted.tokens.InvalidRequest;
import dev.vality.trusted.tokens.validator.conditions.PaymentsConditionsValidator;
import dev.vality.trusted.tokens.validator.conditions.WithdrawalConditionsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static dev.vality.trusted.tokens.constants.ExceptionErrors.INVALID_REQUEST_CONDITIONS;

@Component
@RequiredArgsConstructor
public class ConditionTemplateValidator {

    private final PaymentsConditionsValidator paymentsConditionsValidator;
    private final WithdrawalConditionsValidator withdrawalConditionsValidator;

    public void validate(ConditionTemplate conditionTemplate) throws InvalidRequest {

        if (!conditionTemplate.isSetPaymentsConditions() && !conditionTemplate.isSetWithdrawalsConditions()) {
            throw new InvalidRequest(INVALID_REQUEST_CONDITIONS);
        }
        if (conditionTemplate.isSetPaymentsConditions()) {
            paymentsConditionsValidator.validateConditions(
                    conditionTemplate.getPaymentsConditions().getConditions());
        }
        if (conditionTemplate.isSetWithdrawalsConditions()) {
            withdrawalConditionsValidator.validateConditions(
                    conditionTemplate.getWithdrawalsConditions().getConditions());
        }
    }
}
