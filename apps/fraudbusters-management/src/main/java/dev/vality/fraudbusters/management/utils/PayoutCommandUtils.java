package dev.vality.fraudbusters.management.utils;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandBody;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.damsel.fraudbusters.UserInfo;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PayoutCommandUtils {

    public static Command withType(Command command, CommandType type, String initiator) {
        Command concrete = new Command(command)
                .setCommandType(type)
                .setUserInfo(new UserInfo()
                        .setUserId(initiator));
        markAsPayout(concrete.getCommandBody());
        return concrete;
    }

    /** Without an operation type a command means payment, so payout commands name theirs. */
    private static void markAsPayout(CommandBody commandBody) {
        switch (commandBody.getSetField()) {
            case TEMPLATE -> commandBody.getTemplate().setOperationType(OperationType.payout);
            case REFERENCE -> commandBody.getReference().setOperationType(OperationType.payout);
            case GROUP -> commandBody.getGroup().setOperationType(OperationType.payout);
            case GROUP_REFERENCE -> commandBody.getGroupReference().setOperationType(OperationType.payout);
            default -> throw new IllegalArgumentException("Not a payout command: " + commandBody.getSetField());
        }
    }
}
