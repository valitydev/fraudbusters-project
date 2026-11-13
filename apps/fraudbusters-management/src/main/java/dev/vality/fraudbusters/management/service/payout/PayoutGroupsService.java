package dev.vality.fraudbusters.management.service.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.fraudbusters.management.converter.payment.GroupModelToGroupConverter;
import dev.vality.fraudbusters.management.converter.payment.GroupToCommandConverter;
import dev.vality.fraudbusters.management.converter.payout.PayoutGroupReferenceModelToPayoutGroupReferenceConverter;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupDao;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupReferenceDao;
import dev.vality.fraudbusters.management.domain.GroupModel;
import dev.vality.fraudbusters.management.exception.NotFoundException;
import dev.vality.fraudbusters.management.utils.FilterRequestUtils;
import dev.vality.fraudbusters.management.utils.PagingDataUtils;
import dev.vality.fraudbusters.management.utils.PayoutGroupReferenceUtils;
import dev.vality.swag.fraudbusters.management.model.Group;
import dev.vality.swag.fraudbusters.management.model.GroupsResponse;
import dev.vality.swag.fraudbusters.management.model.PayoutGroupReference;
import dev.vality.swag.fraudbusters.management.model.PayoutGroupReferencesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayoutGroupsService {

    private final PayoutGroupDao payoutGroupDao;
    private final PayoutGroupReferenceDao payoutGroupReferenceDao;
    private final PayoutCommandService payoutCommandService;
    private final GroupToCommandConverter groupToCommandConverter;
    private final GroupModelToGroupConverter groupModelToGroupConverter;
    private final PayoutGroupReferenceModelToPayoutGroupReferenceConverter
            payoutGroupReferenceModelToPayoutGroupReferenceConverter;

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
                        .map(payoutGroupReferenceModelToPayoutGroupReferenceConverter::convert)
                        .toList());
    }

    /** Without party and wallet the group applies to every payout. */
    public List<String> insertGroupReferences(String groupId, List<PayoutGroupReference> references,
                                              String initiator) {
        List<Command> commands = references.stream()
                .map(reference -> PayoutGroupReferenceUtils.toCommand(groupId, reference.getPartyId(),
                        reference.getWalletId()))
                .toList();
        return commands.stream()
                .map(command -> payoutCommandService.sendGroupReference(command, CommandType.CREATE, initiator))
                .toList();
    }

    public String removeGroupReference(String groupId, String partyId, String walletId, String initiator) {
        return payoutCommandService.sendGroupReference(
                PayoutGroupReferenceUtils.toCommand(groupId, partyId, walletId), CommandType.DELETE, initiator);
    }
}
