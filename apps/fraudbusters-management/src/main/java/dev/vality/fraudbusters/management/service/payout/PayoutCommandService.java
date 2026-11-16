package dev.vality.fraudbusters.management.service.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.GroupReference;
import dev.vality.damsel.fraudbusters.TemplateReference;
import dev.vality.fraudbusters.management.config.PayoutProperties;
import dev.vality.fraudbusters.management.service.CommandSender;
import dev.vality.fraudbusters.management.utils.PayoutCommandUtils;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Sends payout configuration to the payout topics, which fraudbusters and management both read. */
@Service
@RequiredArgsConstructor
public class PayoutCommandService {

    private final CommandSender commandSender;

    private final PayoutProperties payoutProperties;

    public String sendTemplate(Command command, CommandType type, String initiator) {
        Command concrete = PayoutCommandUtils.withType(command, type, initiator);
        return commandSender.send(payoutProperties.getTemplateTopic(), concrete,
                concrete.getCommandBody().getTemplate().getId());
    }

    public String sendReference(Command command, CommandType type, String initiator) {
        Command concrete = PayoutCommandUtils.withType(command, type, initiator);
        TemplateReference reference = concrete.getCommandBody().getReference();
        String key = reference.isIsGlobal()
                ? PayoutScope.GLOBAL
                : PayoutScope.generateKey(reference.getPartyId(), reference.getWalletId());
        return commandSender.send(payoutProperties.getReferenceTopic(), concrete, key);
    }

    public String sendGroup(Command command, CommandType type, String initiator) {
        Command concrete = PayoutCommandUtils.withType(command, type, initiator);
        return commandSender.send(payoutProperties.getGroupTopic(), concrete,
                concrete.getCommandBody().getGroup().getGroupId());
    }

    public String sendGroupReference(Command command, CommandType type, String initiator) {
        Command concrete = PayoutCommandUtils.withType(command, type, initiator);
        GroupReference reference = concrete.getCommandBody().getGroupReference();
        return commandSender.send(payoutProperties.getGroupReferenceTopic(), concrete,
                PayoutScope.generateKey(reference.getPartyId(), reference.getWalletId()));
    }
}
