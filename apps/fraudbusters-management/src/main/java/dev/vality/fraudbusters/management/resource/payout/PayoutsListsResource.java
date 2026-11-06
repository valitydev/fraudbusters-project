package dev.vality.fraudbusters.management.resource.payout;

import dev.vality.fraudbusters.management.service.payout.PayoutListsService;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.PayoutsListsApi;
import dev.vality.swag.fraudbusters.management.model.IdResponse;
import dev.vality.swag.fraudbusters.management.model.ListResponse;
import dev.vality.swag.fraudbusters.management.model.PayoutListRecordsResponse;
import dev.vality.swag.fraudbusters.management.model.PayoutRowListRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PayoutsListsResource implements PayoutsListsApi {

    private final PayoutListsService payoutListsService;
    private final UserInfoService userInfoService;

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<PayoutListRecordsResponse> filterPayoutLists(String listType, List<String> listNames,
                                                                       String searchValue, Integer size) {
        log.info("filterPayoutLists initiator: {} listType: {} listNames: {}", userInfoService.getUserName(),
                listType, listNames);
        return ResponseEntity.ok(payoutListsService.filterLists(listType, listNames, searchValue, size));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<ListResponse> getAvailablePayoutListNames() {
        return ResponseEntity.ok(new ListResponse()
                .result(payoutListsService.getAvailableListNames()));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<ListResponse> getCurrentPayoutListNames(String listType) {
        return ResponseEntity.ok(new ListResponse()
                .result(payoutListsService.getCurrentListNames(listType)));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<ListResponse> insertPayoutListRows(PayoutRowListRequest request) {
        String userName = userInfoService.getUserName();
        log.info("insertPayoutListRows initiator: {} listType: {} records: {}", userName, request.getListType(),
                request.getRecords().size());
        return ResponseEntity.ok(new ListResponse()
                .result(payoutListsService.insertRows(request, userName)));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<IdResponse> removePayoutListRow(String listType, String listName, String value,
                                                          String partyId, String walletId) {
        String userName = userInfoService.getUserName();
        log.info("removePayoutListRow initiator: {} listType: {} listName: {}", userName, listType, listName);
        return ResponseEntity.ok(new IdResponse()
                .id(payoutListsService.removeRow(listType, listName, value, partyId, walletId, userName)));
    }
}
