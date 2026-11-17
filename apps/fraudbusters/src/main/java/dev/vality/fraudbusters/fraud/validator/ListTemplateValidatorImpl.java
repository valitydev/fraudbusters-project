package dev.vality.fraudbusters.fraud.validator;

import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.damsel.fraudbusters.Template;
import dev.vality.damsel.fraudbusters.TemplateValidateError;
import dev.vality.damsel.fraudbusters.ValidateTemplateResponse;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.fraud.InspectStorageRegistry;
import dev.vality.fraudbusters.fraud.ListTemplateValidator;
import dev.vality.fraudbusters.fraud.payment.validator.PaymentHistoryFieldValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ListTemplateValidatorImpl implements ListTemplateValidator {

    private final InspectStorageRegistry inspectStorageRegistry;
    private final ErrorCodeFormatValidator errorCodeFormatValidator;
    private final PaymentHistoryFieldValidator paymentHistoryFieldValidator;

    @Override
    public ValidateTemplateResponse validateCompilationTemplate(List<Template> list) {
        return new ValidateTemplateResponse()
                .setErrors(list.stream()
                        .map(this::validateAndMap)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    @Nullable
    private TemplateValidateError validateAndMap(Template template) {
        String templateString = new String(template.getTemplate(), StandardCharsets.UTF_8);
        InspectStorage storage = inspectStorageRegistry.get(template.getOperationType());
        List<String> validate = storage.getTemplateValidator()
                .validate(templateString);
        if (CollectionUtils.isEmpty(validate)) {
            validate = errorCodeFormatValidator.findMisformattedErrorCodes(templateString);
        }
        if (CollectionUtils.isEmpty(validate) && storage.getOperationType() == OperationType.payment) {
            // Payout templates check their history fields in their own validator.
            validate = paymentHistoryFieldValidator.findFieldsMissingInHistory(templateString);
        }
        if (!CollectionUtils.isEmpty(validate)) {
            return new TemplateValidateError()
                    .setId(template.getId())
                    .setReason(validate);
        }
        return null;
    }
}
