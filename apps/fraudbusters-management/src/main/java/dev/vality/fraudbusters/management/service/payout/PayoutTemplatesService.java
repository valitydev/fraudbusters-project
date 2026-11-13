package dev.vality.fraudbusters.management.service.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.damsel.fraudbusters.PaymentServiceSrv;
import dev.vality.damsel.fraudbusters.TemplateValidateError;
import dev.vality.fraudbusters.management.converter.TemplateModelToCommandConverter;
import dev.vality.fraudbusters.management.converter.payment.TemplateModelToTemplateConverter;
import dev.vality.fraudbusters.management.converter.payment.TemplateToCommandConverter;
import dev.vality.fraudbusters.management.dao.payout.template.PayoutTemplateDao;
import dev.vality.fraudbusters.management.domain.TemplateModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.exception.ValidationException;
import dev.vality.fraudbusters.management.utils.FilterRequestUtils;
import dev.vality.swag.fraudbusters.management.model.CreateTemplateResponse;
import dev.vality.swag.fraudbusters.management.model.Template;
import dev.vality.swag.fraudbusters.management.model.TemplatesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutTemplatesService {

    private static final String EMPTY_TEMPLATE = "";

    private final PayoutTemplateDao payoutTemplateDao;
    private final PayoutCommandService payoutCommandService;
    private final PaymentServiceSrv.Iface paymentServiceSrv;
    private final TemplateToCommandConverter templateToCommandConverter;
    private final TemplateModelToCommandConverter templateModelToCommandConverter;
    private final TemplateModelToTemplateConverter templateModelToTemplateConverter;

    public TemplatesResponse filterTemplates(FilterRequest filterRequest) {
        filterRequest.setSearchValue(FilterRequestUtils.prepareSearchValue(filterRequest.getSearchValue()));
        return new TemplatesResponse()
                .count(payoutTemplateDao.countFilterModel(filterRequest.getSearchValue()))
                .result(payoutTemplateDao.filterModel(filterRequest).stream()
                        .map(templateModelToTemplateConverter::destinationToSource)
                        .toList());
    }

    public List<String> getTemplateNames(String idPattern) {
        return payoutTemplateDao.getListNames(idPattern);
    }

    /** Sends the template only when fraudbusters accepts it as a payout template. */
    public CreateTemplateResponse createTemplate(Template template, String initiator) {
        Command command = templateToCommandConverter.convert(template);
        List<TemplateValidateError> errors = validate(command.getCommandBody().getTemplate());
        if (!CollectionUtils.isEmpty(errors)) {
            return new CreateTemplateResponse()
                    .template(template.getTemplate())
                    .errors(errors.get(0).getReason());
        }
        return new CreateTemplateResponse()
                .id(payoutCommandService.sendTemplate(command, CommandType.CREATE, initiator))
                .template(template.getTemplate());
    }

    public String removeTemplate(String id, String initiator) {
        Command command = templateModelToCommandConverter.convert(TemplateModel.builder()
                .id(id)
                .template(EMPTY_TEMPLATE)
                .build());
        return payoutCommandService.sendTemplate(command, CommandType.DELETE, initiator);
    }

    public List<TemplateValidateError> validate(Template template) {
        return validate(templateToCommandConverter.convert(template)
                .getCommandBody()
                .getTemplate());
    }

    private List<TemplateValidateError> validate(dev.vality.damsel.fraudbusters.Template template) {
        try {
            return paymentServiceSrv.validateCompilationTemplate(List.of(new dev.vality.damsel.fraudbusters.Template(
                    template)
                    .setOperationType(OperationType.payout)))
                    .getErrors();
        } catch (TException e) {
            log.error("Error when validate payout template");
            throw new ValidationException(e);
        }
    }
}
