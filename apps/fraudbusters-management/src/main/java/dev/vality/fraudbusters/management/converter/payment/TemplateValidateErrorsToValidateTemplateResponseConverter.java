package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.damsel.fraudbusters.TemplateValidateError;
import dev.vality.swag.fraudbusters.management.model.ErrorTemplate;
import dev.vality.swag.fraudbusters.management.model.ValidateTemplatesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TemplateValidateErrorsToValidateTemplateResponseConverter
        implements Converter<List<TemplateValidateError>, ValidateTemplatesResponse> {

    @Override
    public ValidateTemplatesResponse convert(List<TemplateValidateError> templateValidateErrors) {
        return new ValidateTemplatesResponse()
                .validateResults(templateValidateErrors.stream()
                        .map(templateValidateError -> new ErrorTemplate()
                                .errors(templateValidateError.getReason())
                                .id(templateValidateError.id))
                        .collect(Collectors.toList()));
    }
}
