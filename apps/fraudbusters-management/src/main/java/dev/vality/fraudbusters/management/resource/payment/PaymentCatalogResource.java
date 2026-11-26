package dev.vality.fraudbusters.management.resource.payment;

import dev.vality.fraudbusters.management.service.CatalogMetadataService;
import dev.vality.fraudbusters.management.service.payment.PaymentsListsService;
import dev.vality.swag.fraudbusters.management.api.PaymentsApi;
import dev.vality.swag.fraudbusters.management.model.GroupsMetadata;
import dev.vality.swag.fraudbusters.management.model.PaymentListsMetadata;
import dev.vality.swag.fraudbusters.management.model.PaymentTemplatesMetadata;
import dev.vality.swag.fraudbusters.management.model.WbListRecordsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentCatalogResource implements PaymentsApi {

    private final CatalogMetadataService metadataService;
    private final PaymentsListsService paymentsListsService;

    @Override
    @PreAuthorize("hasRole('fraud-officer')")
    public ResponseEntity<PaymentTemplatesMetadata> getPaymentTemplatesMetadata() {
        return ResponseEntity.ok(metadataService.getPaymentTemplatesMetadata());
    }

    @Override
    @PreAuthorize("hasRole('fraud-officer')")
    public ResponseEntity<GroupsMetadata> getPaymentGroupsMetadata() {
        return ResponseEntity.ok(metadataService.getPaymentGroupsMetadata());
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<PaymentListsMetadata> getPaymentListsMetadata() {
        return ResponseEntity.ok(metadataService.getPaymentListsMetadata());
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<WbListRecordsResponse> filterPaymentCatalogLists(
            String listType, List<String> listNames, String searchValue, String lastId,
            String sortOrder, String sortBy, String sortFieldValue, Integer size) {
        return ResponseEntity.ok(paymentsListsService.filterLists(listType, listNames, searchValue,
                lastId, sortOrder, sortBy, sortFieldValue, size));
    }
}
