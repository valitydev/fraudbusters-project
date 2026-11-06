package dev.vality.fraudbusters.management.service.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.GroupReference;
import dev.vality.damsel.fraudbusters.TemplateReference;
import dev.vality.damsel.fraudbusters.UserInfo;
import dev.vality.fraudbusters.management.service.CommandSender;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Sends payout configuration to the payout topics, which fraudbusters and management both read. */
@Service
@RequiredArgsConstructor
public class PayoutCommandService {

    private final CommandSender commandSender;

    @Value("${kafka.topic.fraudbusters.payout.template}")
    private String templateTopic;
    @Value("${kafka.topic.fraudbusters.payout.reference}")
    private String referenceTopic;
    @Value("${kafka.topic.fraudbusters.payout.group.list}")
    private String groupTopic;
    @Value("${kafka.topic.fraudbusters.payout.group.reference}")
    private String groupReferenceTopic;

    public String sendTemplate(Command command, CommandType type, String initiator) {
        Command concrete = withType(command, type, initiator);
        return commandSender.send(templateTopic, concrete, concrete.getCommandBody().getTemplate().getId());
    }

    public String sendReference(Command command, CommandType type, String initiator) {
        Command concrete = withType(command, type, initiator);
        TemplateReference reference = concrete.getCommandBody().getReference();
        String key = reference.isIsGlobal()
                ? PayoutScope.GLOBAL
                : PayoutScope.generateKey(reference.getPartyId(), reference.getWalletId());
        return commandSender.send(referenceTopic, concrete, key);
    }

    public String sendGroup(Command command, CommandType type, String initiator) {
        Command concrete = withType(command, type, initiator);
        return commandSender.send(groupTopic, concrete, concrete.getCommandBody().getGroup().getGroupId());
    }

    public String sendGroupReference(Command command, CommandType type, String initiator) {
        Command concrete = withType(command, type, initiator);
        GroupReference reference = concrete.getCommandBody().getGroupReference();
        return commandSender.send(groupReferenceTopic, concrete,
                PayoutScope.generateKey(reference.getPartyId(), reference.getWalletId()));
    }

    private static Command withType(Command command, CommandType type, String initiator) {
        return new Command(command)
                .setCommandType(type)
                .setUserInfo(new UserInfo()
                        .setUserId(initiator));
    }
}
