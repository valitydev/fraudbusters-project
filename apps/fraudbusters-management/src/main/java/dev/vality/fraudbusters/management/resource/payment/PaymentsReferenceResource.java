package dev.vality.fraudbusters.management.resource.payment;

import dev.vality.fraudbusters.management.converter.payment.DefaultPaymentReferenceModelToPaymentReferenceConverter;
import dev.vality.fraudbusters.management.converter.payment.PaymentReferenceModelToPaymentReferenceConverter;
import dev.vality.fraudbusters.management.dao.payment.DefaultPaymentReferenceDaoImpl;
import dev.vality.fraudbusters.management.dao.payment.reference.PaymentReferenceDao;
import dev.vality.fraudbusters.management.domain.payment.DefaultPaymentReferenceModel;
import dev.vality.fraudbusters.management.domain.payment.PaymentReferenceModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.filter.UnknownPaymentTemplateInReferenceFilter;
import dev.vality.fraudbusters.management.service.payment.PaymentsDefaultReferenceService;
import dev.vality.fraudbusters.management.service.payment.PaymentsReferenceService;
import dev.vality.fraudbusters.management.utils.FilterRequestUtils;
import dev.vality.fraudbusters.management.utils.PagingDataUtils;
import dev.vality.fraudbusters.management.utils.PaymentUnknownTemplateFinder;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.PaymentsReferencesApi;
import dev.vality.swag.fraudbusters.management.model.IdResponse;
import dev.vality.swag.fraudbusters.management.model.ListResponse;
import dev.vality.swag.fraudbusters.management.model.PaymentReference;
import dev.vality.swag.fraudbusters.management.model.ReferencesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentsReferenceResource implements PaymentsReferencesApi {

    private final PaymentReferenceDao referenceDao;
    private final UserInfoService userInfoService;
    private final PaymentReferenceModelToPaymentReferenceConverter modelToPaymentReferenceConverter;
    private final PaymentsReferenceService paymentsReferenceService;
    private final DefaultPaymentReferenceDaoImpl defaultPaymentReferenceDao;
    private final PaymentsDefaultReferenceService paymentsDefaultReferenceService;
    private final DefaultPaymentReferenceModelToPaymentReferenceConverter defaultModelToPaymentReferenceConverter;
    private final PaymentUnknownTemplateFinder unknownTemplateFinder;
    private final UnknownPaymentTemplateInReferenceFilter templateInReferenceFilter;

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<ReferencesResponse> filterDefaultReferences(String lastId, String sortOrder,
                                                                      String searchValue, String sortBy,
                                                                      String sortFieldValue,
                                                                      Integer size) {
        var filterRequest = FilterRequest.builder()
                .searchValue(searchValue)
                .lastId(lastId)
                .sortFieldValue(sortFieldValue)
                .size(size)
                .sortBy(sortBy)
                .sortOrder(PagingDataUtils.getSortOrder(sortOrder))
                .build();
        log.info("filterReferences initiator: {} filterRequest: {}", userInfoService.getUserName(), filterRequest);
        filterRequest.setSearchValue(FilterRequestUtils.prepareSearchValue(filterRequest.getSearchValue()));
        List<DefaultPaymentReferenceModel> paymentReferenceModels =
                defaultPaymentReferenceDao.filterReferences(filterRequest);
        Integer count = defaultPaymentReferenceDao.countFilterModel(searchValue);
        return ResponseEntity.ok().body(new ReferencesResponse()
                .count(count)
                .result(paymentReferenceModels.stream()
                        .map(defaultModelToPaymentReferenceConverter::convert)
                        .collect(Collectors.toList())));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> insertDefaultReference(PaymentReference paymentReference) {
        log.info("insertDefaultReference initiator: {} referenceModels: {}", userInfoService.getUserName(),
                paymentReference);
        String uid = paymentsDefaultReferenceService.insertDefaultReference(paymentReference);
        return ResponseEntity.ok().body(new IdResponse()
                .id(uid));
    }


    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> removeDefaultReference(String id) {
        log.info("removeDefaultReference initiator: {} id: {}", userInfoService.getUserName(), id);
        defaultPaymentReferenceDao.remove(id);
        return ResponseEntity.ok().body(new IdResponse()
                .id(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<ReferencesResponse> filterReferences(String lastId, String sortOrder,
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
        log.info("filterReferences initiator: {} filterRequest: {}", userInfoService.getUserName(), filterRequest);
        List<PaymentReferenceModel> paymentReferenceModels = referenceDao.filterReferences(filterRequest);
        Integer count = referenceDao.countFilterModel(filterRequest.getSearchValue());
        return ResponseEntity.ok().body(new ReferencesResponse()
                .count(count)
                .result(paymentReferenceModels.stream()
                        .map(modelToPaymentReferenceConverter::convert)
                        .collect(Collectors.toList())));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<ListResponse> insertReferences(List<PaymentReference> paymentReference) {
        String userName = userInfoService.getUserName();
        log.info("insertReference initiator: {} referenceModels: {}", userName, paymentReference);
        List<String> unknownTemplates =
                unknownTemplateFinder.find(paymentReference, templateInReferenceFilter);
        if (!CollectionUtils.isEmpty(unknownTemplates)) {
            return new ResponseEntity<>(
                    new ListResponse()
                            .result(unknownTemplates),
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }
        List<String> ids = paymentsReferenceService.insertReferences(paymentReference, userName);
        return ResponseEntity.ok().body(new ListResponse()
                .result(ids));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> removeReference(String id) {
        String userName = userInfoService.getUserName();
        log.info("removeReference initiator: {} id: {}", userName, id);
        String commandSendDeletedId = paymentsReferenceService.removeReference(id, userName);
        return ResponseEntity.ok().body(new IdResponse()
                .id(commandSendDeletedId));
    }

}
