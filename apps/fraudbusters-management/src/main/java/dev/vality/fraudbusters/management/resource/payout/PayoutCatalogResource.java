package dev.vality.fraudbusters.management.resource.payout;

import dev.vality.fraudbusters.management.config.FraudAuthorization;
import dev.vality.fraudbusters.management.service.CatalogMetadataService;
import dev.vality.swag.fraudbusters.management.api.PayoutsApi;
import dev.vality.swag.fraudbusters.management.model.GroupsMetadata;
import dev.vality.swag.fraudbusters.management.model.PayoutListsMetadata;
import dev.vality.swag.fraudbusters.management.model.PayoutTemplatesMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PayoutCatalogResource implements PayoutsApi {

    private final CatalogMetadataService metadataService;

    @Override
    @PreAuthorize(FraudAuthorization.FRAUD_OFFICER_ONLY)
    public ResponseEntity<PayoutTemplatesMetadata> getPayoutTemplatesMetadata() {
        return ResponseEntity.ok(metadataService.getPayoutTemplatesMetadata());
    }

    @Override
    @PreAuthorize(FraudAuthorization.FRAUD_OFFICER_ONLY)
    public ResponseEntity<GroupsMetadata> getPayoutGroupsMetadata() {
        return ResponseEntity.ok(metadataService.getPayoutGroupsMetadata());
    }

    @Override
    @PreAuthorize(FraudAuthorization.FRAUD_MONITORING_OR_OFFICER)
    public ResponseEntity<PayoutListsMetadata> getPayoutListsMetadata() {
        return ResponseEntity.ok(metadataService.getPayoutListsMetadata());
    }
}
