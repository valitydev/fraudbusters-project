package dev.vality.fraudbusters.management.service.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.UserInfo;
import dev.vality.fraudbusters.management.converter.payment.GroupToCommandConverter;
import dev.vality.fraudbusters.management.converter.payment.PaymentGroupReferenceModelToCommandConverter;
import dev.vality.fraudbusters.management.service.CommandSender;
import dev.vality.swag.fraudbusters.management.model.Group;
import dev.vality.swag.fraudbusters.management.model.GroupReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
public class PaymentGroupCommandService {

    private final CommandSender commandSender;
    private final String topic;
    private final GroupToCommandConverter groupToCommandConverter;
    private final PaymentGroupReferenceModelToCommandConverter groupReferenceToCommandConverter;

    public String sendCommandSync(Command command) {
        String key = command.getCommandBody().getGroup().getGroupId();
        return commandSender.send(topic, command, key);
    }

    public Command initDeleteGroupReferenceCommand(String id, String initiator) {
        return groupToCommandConverter.convert(new Group()
                .groupId(id)
                .priorityTemplates(new ArrayList<>()))
                .setCommandType(CommandType.DELETE)
                .setUserInfo(new UserInfo(initiator));
    }

    public Command initDeleteGroupReferenceCommand(String partyId, String shopId, String groupId, String initiator) {
        var groupReferenceModel = new GroupReference()
                .partyId(partyId)
                .shopId(shopId)
                .groupId(groupId);
        var command = convertReferenceModel(groupReferenceModel, groupId);
        command.setCommandType(CommandType.DELETE);
        command.setUserInfo(new UserInfo()
                .setUserId(initiator));
        return command;
    }

    public Command convertReferenceModel(GroupReference groupReferenceModel, String groupId) {
        var command = groupReferenceToCommandConverter.convert(groupReferenceModel);
        command.getCommandBody().getGroupReference().setGroupId(groupId);
        return command;
    }

    public Command initCreateCommand(Command command, String initiator) {
        return new Command(command)
                .setCommandType(CommandType.CREATE)
                .setUserInfo(new UserInfo()
                        .setUserId(initiator));
    }
}
