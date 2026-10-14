package dev.vality.fraudbusters.management.resource.payment;

import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.TemplateValidateError;
import dev.vality.fraudbusters.management.converter.payment.TemplateValidateErrorsToValidateTemplateResponseConverter;
import dev.vality.fraudbusters.management.dao.TemplateDao;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.service.TemplateCommandService;
import dev.vality.fraudbusters.management.service.iface.ValidationTemplateService;
import dev.vality.fraudbusters.management.service.payment.PaymentsTemplatesService;
import dev.vality.fraudbusters.management.utils.CommandMapper;
import dev.vality.fraudbusters.management.utils.PagingDataUtils;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.PaymentsTemplatesApi;
import dev.vality.swag.fraudbusters.management.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentsTemplatesResource implements PaymentsTemplatesApi {

    private final TemplateCommandService paymentTemplateCommandService;
    private final ValidationTemplateService paymentValidationService;
    private final TemplateDao paymentTemplateDao;
    private final UserInfoService userInfoService;
    private final CommandMapper commandMapper;
    private final PaymentsTemplatesService paymentsTemplatesService;
    private final TemplateValidateErrorsToValidateTemplateResponseConverter errorsToValidateTemplateResponseConverter;

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<TemplatesResponse> filterTemplates(String lastId, String sortOrder,
                                                             String searchValue, String sortBy,
                                                             String sortFieldValue, Integer size) {
        var filterRequest = FilterRequest.builder()
                .searchValue(searchValue)
                .lastId(lastId)
                .sortFieldValue(sortFieldValue)
                .size(size)
                .sortBy(sortBy)
                .sortOrder(PagingDataUtils.getSortOrder(sortOrder))
                .build();
        String userName = userInfoService.getUserName();
        log.info("filterTemplates initiator: {} filterRequest: {}", userName, filterRequest);
        TemplatesResponse templatesResponse = paymentsTemplatesService.filterTemplates(filterRequest);
        return ResponseEntity.ok().body(templatesResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<ListResponse> getTemplateNames(String regexpName) {
        log.info("getTemplatesName initiator: {} regexpName: {}", userInfoService.getUserName(), regexpName);
        List<String> list = paymentTemplateDao.getListNames(regexpName);
        return ResponseEntity.ok().body(new ListResponse()
                .result(list));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<CreateTemplateResponse> insertTemplate(
            dev.vality.swag.fraudbusters.management.model.Template template) {
        String userName = userInfoService.getUserName();
        log.info("insertTemplate initiator: {} templateModel: {}", userName,
                template);
        return ResponseEntity.ok().body(paymentsTemplatesService.createTemplate(template, userName));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> removeTemplate(String id) {
        String userName = userInfoService.getUserName();
        log.info("removeTemplate initiator: {} id: {}", userName, id);
        var command = paymentTemplateCommandService.createTemplateCommandById(id);
        String messageId = paymentTemplateCommandService
                .sendCommandSync(commandMapper.mapToConcreteCommand(userName, command, CommandType.DELETE));
        return ResponseEntity.ok().body(
                new IdResponse().id(messageId)
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<ValidateTemplatesResponse> validateTemplate(Template template) {
        log.info("validateTemplate initiator: {} templateModel: {}", userInfoService.getUserName(),
                template);
        List<TemplateValidateError> templateValidateErrors = paymentValidationService.validateTemplate(
                new dev.vality.damsel.fraudbusters.Template()
                        .setId(template.getId())
                        .setTemplate(template.getTemplate().getBytes()));
        log.info("validateTemplate result: {}", templateValidateErrors);
        return ResponseEntity.ok().body(errorsToValidateTemplateResponseConverter.convert(templateValidateErrors));
    }

}
