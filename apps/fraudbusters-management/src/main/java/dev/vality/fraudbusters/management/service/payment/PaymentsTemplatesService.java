package dev.vality.fraudbusters.management.service.payment;

import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.TemplateValidateError;
import dev.vality.damsel.fraudbusters.UserInfo;
import dev.vality.fraudbusters.management.converter.payment.TemplateModelToTemplateConverter;
import dev.vality.fraudbusters.management.converter.payment.TemplateToCommandConverter;
import dev.vality.fraudbusters.management.dao.TemplateDao;
import dev.vality.fraudbusters.management.domain.TemplateModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.service.TemplateCommandService;
import dev.vality.fraudbusters.management.service.iface.ValidationTemplateService;
import dev.vality.fraudbusters.management.utils.FilterRequestUtils;
import dev.vality.swag.fraudbusters.management.model.CreateTemplateResponse;
import dev.vality.swag.fraudbusters.management.model.Template;
import dev.vality.swag.fraudbusters.management.model.TemplatesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentsTemplatesService {

    private final TemplateDao paymentTemplateDao;
    private final TemplateModelToTemplateConverter templateModelToTemplateConverter;
    private final TemplateToCommandConverter templateModelToCommandConverter;
    private final TemplateCommandService paymentTemplateCommandService;
    private final ValidationTemplateService paymentValidationService;

    public TemplatesResponse filterTemplates(FilterRequest filterRequest) {
        filterRequest.setSearchValue(FilterRequestUtils.prepareSearchValue(filterRequest.getSearchValue()));
        List<TemplateModel> templateModels = paymentTemplateDao.filterModel(filterRequest);
        Integer count = paymentTemplateDao.countFilterModel(filterRequest.getSearchValue());
        return new TemplatesResponse()
                .count(count)
                .result(templateModels.stream()
                        .map(templateModelToTemplateConverter::destinationToSource)
                        .collect(Collectors.toList())
                );
    }


    public CreateTemplateResponse createTemplate(Template template, String initiator) {
        var command = templateModelToCommandConverter.convert(template);
        List<TemplateValidateError> templateValidateErrors = paymentValidationService.validateTemplate(
                command.getCommandBody().getTemplate()
        );
        if (!CollectionUtils.isEmpty(templateValidateErrors)) {
            return new CreateTemplateResponse()
                    .template(template.getTemplate())
                    .errors(templateValidateErrors.get(0).getReason());
        }
        command.setCommandType(CommandType.CREATE);
        command.setUserInfo(new UserInfo()
                .setUserId(initiator));
        String idMessage = paymentTemplateCommandService.sendCommandSync(command);
        return new CreateTemplateResponse()
                .id(idMessage)
                .template(template.getTemplate());
    }
}
