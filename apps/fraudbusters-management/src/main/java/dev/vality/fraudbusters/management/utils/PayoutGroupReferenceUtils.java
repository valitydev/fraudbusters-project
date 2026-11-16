package dev.vality.fraudbusters.management.utils;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandBody;
import dev.vality.damsel.fraudbusters.GroupReference;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

@UtilityClass
public class PayoutGroupReferenceUtils {

    public static Command toCommand(String groupId, String partyId, String walletId) {
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

}
