package dev.vality.fraudbusters.management.resource.payout;

import dev.vality.fraudbusters.management.service.payout.PayoutReferencesService;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.PayoutsReferencesApi;
import dev.vality.swag.fraudbusters.management.model.IdResponse;
import dev.vality.swag.fraudbusters.management.model.ListResponse;
import dev.vality.swag.fraudbusters.management.model.PayoutReference;
import dev.vality.swag.fraudbusters.management.model.PayoutReferencesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PayoutsReferencesResource implements PayoutsReferencesApi {

    private final PayoutReferencesService payoutReferencesService;
    private final UserInfoService userInfoService;

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<PayoutReferencesResponse> filterPayoutReferences(String searchValue, Boolean isGlobal,
                                                                           Integer size) {
        log.info("filterPayoutReferences initiator: {} searchValue: {} isGlobal: {}",
                userInfoService.getUserName(), searchValue, isGlobal);
        return ResponseEntity.ok(payoutReferencesService.filterReferences(searchValue, isGlobal, size));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<ListResponse> insertPayoutReferences(List<PayoutReference> payoutReferences) {
        String userName = userInfoService.getUserName();
        log.info("insertPayoutReferences initiator: {} references: {}", userName, payoutReferences);
        return ResponseEntity.ok(new ListResponse()
                .result(payoutReferencesService.insertReferences(payoutReferences, userName)));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> removePayoutReference(String partyId, String walletId, Boolean isGlobal) {
        String userName = userInfoService.getUserName();
        log.info("removePayoutReference initiator: {} partyId: {} walletId: {} isGlobal: {}",
                userName, partyId, walletId, isGlobal);
        return ResponseEntity.ok(new IdResponse()
                .id(payoutReferencesService.removeReference(partyId, walletId, isGlobal, userName)));
    }
}
