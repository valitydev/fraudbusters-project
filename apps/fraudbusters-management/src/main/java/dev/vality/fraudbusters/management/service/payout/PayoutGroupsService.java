package dev.vality.fraudbusters.management.service.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandBody;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.GroupReference;
import dev.vality.fraudbusters.management.converter.payment.GroupModelToGroupConverter;
import dev.vality.fraudbusters.management.converter.payment.GroupToCommandConverter;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupDao;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupReferenceDao;
import dev.vality.fraudbusters.management.domain.GroupModel;
import dev.vality.fraudbusters.management.domain.payout.PayoutGroupReferenceModel;
import dev.vality.fraudbusters.management.exception.NotFoundException;
import dev.vality.fraudbusters.management.utils.DateTimeUtils;
import dev.vality.fraudbusters.management.utils.FilterRequestUtils;
import dev.vality.fraudbusters.management.utils.PagingDataUtils;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import dev.vality.swag.fraudbusters.management.model.Group;
import dev.vality.swag.fraudbusters.management.model.GroupsResponse;
import dev.vality.swag.fraudbusters.management.model.PayoutGroupReference;
import dev.vality.swag.fraudbusters.management.model.PayoutGroupReferencesResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PayoutGroupsService {

    private final PayoutGroupDao payoutGroupDao;
    private final PayoutGroupReferenceDao payoutGroupReferenceDao;
    private final PayoutCommandService payoutCommandService;
    private final GroupToCommandConverter groupToCommandConverter;
    private final GroupModelToGroupConverter groupModelToGroupConverter;

    public GroupsResponse filterGroups(String searchValue) {
        List<Group> groups = payoutGroupDao.filterGroup(FilterRequestUtils.prepareSearchValue(searchValue)).stream()
                .map(groupModelToGroupConverter::convert)
                .toList();
        return new GroupsResponse()
                .count(groups.size())
                .result(groups);
    }

    public Group getGroup(String groupId) {
        GroupModel group = payoutGroupDao.getById(groupId);
        if (group == null) {
            throw new NotFoundException("Payout group not found: " + groupId);
        }
        return groupModelToGroupConverter.convert(group);
    }

    public String insertGroup(Group group, String initiator) {
        if (!StringUtils.hasText(group.getGroupId())) {
            throw new IllegalArgumentException("groupId is required");
        }
        return payoutCommandService.sendGroup(groupToCommandConverter.convert(group), CommandType.CREATE, initiator);
    }

    public String removeGroup(String groupId, String initiator) {
        Command command = groupToCommandConverter.convert(new Group()
                .groupId(groupId)
                .priorityTemplates(new ArrayList<>()));
        return payoutCommandService.sendGroup(command, CommandType.DELETE, initiator);
    }

    public PayoutGroupReferencesResponse filterGroupReferences(String searchValue, String lastId, String sortOrder,
                                                               Integer size) {
        String pattern = FilterRequestUtils.prepareSearchValue(searchValue);
        return new PayoutGroupReferencesResponse()
                .count(payoutGroupReferenceDao.countFilter(pattern))
                .result(payoutGroupReferenceDao.filter(pattern, lastId, PagingDataUtils.getSortOrder(sortOrder), size)
                        .stream()
                        .map(PayoutGroupsService::toApi)
                        .toList());
    }

    /** Without party and wallet the group applies to every payout. */
    public List<String> insertGroupReferences(String groupId, List<PayoutGroupReference> references,
                                              String initiator) {
        List<Command> commands = references.stream()
                .map(reference -> toCommand(groupId, reference.getPartyId(), reference.getWalletId()))
                .toList();
        return commands.stream()
                .map(command -> payoutCommandService.sendGroupReference(command, CommandType.CREATE, initiator))
                .toList();
    }

    public String removeGroupReference(String groupId, String partyId, String walletId, String initiator) {
        return payoutCommandService.sendGroupReference(toCommand(groupId, partyId, walletId), CommandType.DELETE,
                initiator);
    }

    private static Command toCommand(String groupId, String partyId, String walletId) {
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("groupId is required");
        }
        PayoutScope.requirePartyForWallet(partyId, walletId);
        GroupReference reference = new GroupReference()
                .setGroupId(groupId);
        if (StringUtils.hasText(partyId)) {
            reference.setPartyId(partyId);
            if (StringUtils.hasText(walletId)) {
                reference.setWalletId(walletId);
            }
        }
        return new Command()
                .setCommandBody(CommandBody.group_reference(reference));
    }

    private static PayoutGroupReference toApi(PayoutGroupReferenceModel model) {
        return new PayoutGroupReference()
                .id(model.getId())
                .groupId(model.getGroupId())
                .partyId(StringUtils.hasText(model.getPartyId()) ? model.getPartyId() : null)
                .walletId(StringUtils.hasText(model.getWalletId()) ? model.getWalletId() : null)
                .lastUpdateDate(DateTimeUtils.toDate(model.getLastUpdateDate()))
                .modifiedByUser(model.getModifiedByUser());
    }
}
