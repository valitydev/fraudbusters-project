package dev.vality.fraudbusters.management.resource.payment;

import dev.vality.fraudbusters.management.converter.payment.GroupModelToGroupConverter;
import dev.vality.fraudbusters.management.converter.payment.GroupToCommandConverter;
import dev.vality.fraudbusters.management.converter.payment.PaymentGroupReferenceModelToGroupReferenceConverter;
import dev.vality.fraudbusters.management.dao.payment.group.PaymentGroupDao;
import dev.vality.fraudbusters.management.dao.payment.group.PaymentGroupReferenceDao;
import dev.vality.fraudbusters.management.domain.GroupModel;
import dev.vality.fraudbusters.management.domain.payment.PaymentGroupReferenceModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.service.payment.PaymentGroupCommandService;
import dev.vality.fraudbusters.management.service.payment.PaymentGroupReferenceCommandService;
import dev.vality.fraudbusters.management.utils.PagingDataUtils;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.PaymentsGroupsApi;
import dev.vality.swag.fraudbusters.management.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentGroupsResource implements PaymentsGroupsApi {

    private final PaymentGroupDao groupDao;
    private final PaymentGroupReferenceDao referenceDao;
    private final UserInfoService userInfoService;
    private final PaymentGroupReferenceCommandService paymentGroupReferenceService;
    private final PaymentGroupCommandService paymentGroupCommandService;
    private final GroupToCommandConverter groupToCommandConverter;
    private final PaymentGroupReferenceModelToGroupReferenceConverter referenceModelToGroupReferenceConverter;
    private final GroupModelToGroupConverter groupModelToGroupConverter;

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<GroupsResponse> filterGroups(String searchValue) {
        log.info("-> filterGroup initiator: {} searchValue: {}", userInfoService.getUserName(), searchValue);
        List<GroupModel> groupModels = groupDao.filterGroup(searchValue);
        log.info("filterGroup groupModels: {}", groupModels);
        return ResponseEntity.ok().body(new GroupsResponse()
                .result(groupModels.stream()
                        .map(groupModelToGroupConverter::convert)
                        .collect(Collectors.toList())
                ));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<GroupsReferencesResponse> filterGroupsReferences(String sortOrder,
                                                                           String searchValue,
                                                                           String sortBy,
                                                                           String sortFieldValue,
                                                                           Integer size, String lastId) {
        var filterRequest = FilterRequest.builder()
                .searchValue(searchValue)
                .lastId(lastId)
                .sortFieldValue(sortFieldValue)
                .size(size)
                .sortBy(sortBy)
                .sortOrder(PagingDataUtils.getSortOrder(sortOrder))
                .build();
        log.info("filterReference idRegexp: {}", filterRequest.getSearchValue());
        List<PaymentGroupReferenceModel> listByTemplateId = referenceDao.filterReference(filterRequest);
        Integer count = referenceDao.countFilterReference(filterRequest.getSearchValue());
        return ResponseEntity.ok().body(new GroupsReferencesResponse()
                .count(count)
                .result(listByTemplateId.stream()
                        .map(referenceModelToGroupReferenceConverter::convert)
                        .collect(Collectors.toList()))
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<Group> getGroup(String id) {
        log.info("getGroupById initiator: {} groupId: {}", userInfoService.getUserName(), id);
        var groupModel = groupDao.getById(id);
        if (groupModel == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(groupModelToGroupConverter.convert(groupModel));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> insertGroup(Group group) {
        String userName = userInfoService.getUserName();
        log.info("insertTemplate initiator: {} group: {}", userName, group);
        var command = groupToCommandConverter.convert(group);
        command = paymentGroupCommandService.initCreateCommand(command, userName);
        String idMessage = paymentGroupCommandService.sendCommandSync(command);
        return ResponseEntity.ok().body(
                new IdResponse().id(idMessage)
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<ListResponse> insertGroupReferences(String id, List<GroupReference> groupReference) {
        String userName = userInfoService.getUserName();
        log.info("insertReference initiator: {} referenceModels: {}", userName, groupReference);
        List<String> ids = groupReference.stream()
                .map(reference -> paymentGroupCommandService.convertReferenceModel(reference, id))
                .map(command -> paymentGroupCommandService.initCreateCommand(command, userName))
                .map(paymentGroupReferenceService::sendCommandSync)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(new ListResponse()
                .result(ids));
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<IdResponse> removeGroup(String id) {
        String userName = userInfoService.getUserName();
        log.info("removeGroup initiator: {} id: {}", userName, id);
        var command = paymentGroupCommandService.initDeleteGroupReferenceCommand(id, userName);
        String idMessage = paymentGroupCommandService.sendCommandSync(command);
        return ResponseEntity.ok().body(
                new IdResponse().id(idMessage)
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<String> removeGroupReference(String id, String partyId,
                                                       String shopId, String groupId) {
        String userName = userInfoService.getUserName();
        log.info("removeGroupReference initiator: {} groupId: {} partyId: {} shopId: {}",
                userName, groupId, partyId, shopId);
        var command = paymentGroupCommandService.initDeleteGroupReferenceCommand(partyId, shopId, groupId, userName);
        String resultId = paymentGroupReferenceService.sendCommandSync(command);
        log.info("removeGroupReference sendCommand id: {}", resultId);
        return ResponseEntity.ok().body(resultId);
    }

}
