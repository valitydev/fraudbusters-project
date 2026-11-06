package dev.vality.fraudbusters.management.resource.payout;

import dev.vality.fraudbusters.management.converter.payment.TemplateValidateErrorsToValidateTemplateResponseConverter;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.service.payout.PayoutTemplatesService;
import dev.vality.fraudbusters.management.utils.PagingDataUtils;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.PayoutsTemplatesApi;
import dev.vality.swag.fraudbusters.management.model.CreateTemplateResponse;
import dev.vality.swag.fraudbusters.management.model.IdResponse;
import dev.vality.swag.fraudbusters.management.model.ListResponse;
import dev.vality.swag.fraudbusters.management.model.Template;
import dev.vality.swag.fraudbusters.management.model.TemplatesResponse;
import dev.vality.swag.fraudbusters.management.model.ValidateTemplatesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PayoutsTemplatesResource implements PayoutsTemplatesApi {

    private final PayoutTemplatesService payoutTemplatesService;
    private final UserInfoService userInfoService;
    private final TemplateValidateErrorsToValidateTemplateResponseConverter errorsToValidateTemplateResponseConverter;

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<TemplatesResponse> filterPayoutTemplates(String lastId, String sortOrder,
                                                                   String searchValue, Integer size) {
        var filterRequest = FilterRequest.builder()
                .lastId(lastId)
                .sortOrder(PagingDataUtils.getSortOrder(sortOrder))
                .searchValue(searchValue)
                .size(size)
                .build();
        log.info("filterPayoutTemplates initiator: {} filterRequest: {}", userInfoService.getUserName(),
                filterRequest);
        return ResponseEntity.ok(payoutTemplatesService.filterTemplates(filterRequest));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<ListResponse> getPayoutTemplateNames(String regexpName) {
        log.info("getPayoutTemplateNames initiator: {} regexpName: {}", userInfoService.getUserName(), regexpName);
        return ResponseEntity.ok(new ListResponse()
                .result(payoutTemplatesService.getTemplateNames(regexpName)));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<CreateTemplateResponse> insertPayoutTemplate(Template template) {
        String userName = userInfoService.getUserName();
        log.info("insertPayoutTemplate initiator: {} template: {}", userName, template);
        return ResponseEntity.ok(payoutTemplatesService.createTemplate(template, userName));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> removePayoutTemplate(String id) {
        String userName = userInfoService.getUserName();
        log.info("removePayoutTemplate initiator: {} id: {}", userName, id);
        return ResponseEntity.ok(new IdResponse()
                .id(payoutTemplatesService.removeTemplate(id, userName)));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<ValidateTemplatesResponse> validatePayoutTemplate(Template template) {
        log.info("validatePayoutTemplate initiator: {} template: {}", userInfoService.getUserName(), template);
        return ResponseEntity.ok(errorsToValidateTemplateResponseConverter.convert(
                payoutTemplatesService.validate(template)));
    }
}
