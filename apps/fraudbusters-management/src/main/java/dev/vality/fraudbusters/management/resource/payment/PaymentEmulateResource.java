package dev.vality.fraudbusters.management.resource.payment;

import dev.vality.fraudbusters.management.converter.payment.TemplateModelToTemplateConverter;
import dev.vality.fraudbusters.management.domain.TemplateModel;
import dev.vality.fraudbusters.management.service.payment.PaymentEmulateService;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.PaymentsEmulationsApi;
import dev.vality.swag.fraudbusters.management.model.EmulateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentEmulateResource implements PaymentsEmulationsApi {

    private final PaymentEmulateService paymentEmulateService;
    private final UserInfoService userInfoService;
    private final TemplateModelToTemplateConverter templateModelToTemplateConverterImpl;

    @Override
    @PreAuthorize("hasAnyRole('fraud-support', 'fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<EmulateResponse> getTemplatesFlow(String partyId,
                                                            String shopId) {
        log.info("EmulateResource getRulesByPartyAndShop initiator: {} partyId: {} shopId: {}",
                userInfoService.getUserName(), partyId, shopId);
        List<TemplateModel> resultModels = paymentEmulateService.getTemplatesFlow(partyId, shopId);
        log.info("EmulateResource getRulesByPartyAndShop result: {}", resultModels);
        return ResponseEntity.ok().body(new EmulateResponse()
                .result(resultModels.stream()
                        .filter(Objects::nonNull)
                        .map(templateModelToTemplateConverterImpl::destinationToSource)
                        .collect(Collectors.toList()))
        );
    }

}
