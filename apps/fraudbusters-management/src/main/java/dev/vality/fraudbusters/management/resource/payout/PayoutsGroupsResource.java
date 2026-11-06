package dev.vality.fraudbusters.management.resource.payout;

import dev.vality.fraudbusters.management.service.payout.PayoutGroupsService;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.PayoutsGroupsApi;
import dev.vality.swag.fraudbusters.management.model.Group;
import dev.vality.swag.fraudbusters.management.model.GroupsResponse;
import dev.vality.swag.fraudbusters.management.model.IdResponse;
import dev.vality.swag.fraudbusters.management.model.ListResponse;
import dev.vality.swag.fraudbusters.management.model.PayoutGroupReference;
import dev.vality.swag.fraudbusters.management.model.PayoutGroupReferencesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PayoutsGroupsResource implements PayoutsGroupsApi {

    private final PayoutGroupsService payoutGroupsService;
    private final UserInfoService userInfoService;

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<GroupsResponse> filterPayoutGroups(String searchValue) {
        log.info("filterPayoutGroups initiator: {} searchValue: {}", userInfoService.getUserName(), searchValue);
        return ResponseEntity.ok(payoutGroupsService.filterGroups(searchValue));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<Group> getPayoutGroup(String groupId) {
        log.info("getPayoutGroup initiator: {} groupId: {}", userInfoService.getUserName(), groupId);
        return ResponseEntity.ok(payoutGroupsService.getGroup(groupId));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> insertPayoutGroup(Group group) {
        String userName = userInfoService.getUserName();
        log.info("insertPayoutGroup initiator: {} group: {}", userName, group);
        return ResponseEntity.ok(new IdResponse()
                .id(payoutGroupsService.insertGroup(group, userName)));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> removePayoutGroup(String groupId) {
        String userName = userInfoService.getUserName();
        log.info("removePayoutGroup initiator: {} groupId: {}", userName, groupId);
        return ResponseEntity.ok(new IdResponse()
                .id(payoutGroupsService.removeGroup(groupId, userName)));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<PayoutGroupReferencesResponse> filterPayoutGroupReferences(String searchValue,
                                                                                     Integer size) {
        log.info("filterPayoutGroupReferences initiator: {} searchValue: {}", userInfoService.getUserName(),
                searchValue);
        return ResponseEntity.ok(payoutGroupsService.filterGroupReferences(searchValue, size));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<ListResponse> insertPayoutGroupReferences(String groupId,
                                                                    List<PayoutGroupReference> references) {
        String userName = userInfoService.getUserName();
        log.info("insertPayoutGroupReferences initiator: {} groupId: {} references: {}", userName, groupId,
                references);
        return ResponseEntity.ok(new ListResponse()
                .result(payoutGroupsService.insertGroupReferences(groupId, references, userName)));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> removePayoutGroupReference(String groupId, String partyId, String walletId) {
        String userName = userInfoService.getUserName();
        log.info("removePayoutGroupReference initiator: {} groupId: {} partyId: {} walletId: {}",
                userName, groupId, partyId, walletId);
        return ResponseEntity.ok(new IdResponse()
                .id(payoutGroupsService.removeGroupReference(groupId, partyId, walletId, userName)));
    }
}
