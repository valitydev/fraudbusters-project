package dev.vality.fraudbusters.util;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.OperationType;

public final class OperationTypeUtil {

    private OperationTypeUtil() {
    }

    public static boolean isPayout(Command command) {
        return declared(command) == OperationType.payout;
    }

    /** The topic defines the operation type; a command may only confirm it, not declare another one. */
    public static boolean declaresOther(Command command, OperationType topicType) {
        OperationType declared = declared(command);
        return declared != null && declared != topicType;
    }

    private static OperationType declared(Command command) {
        if (command == null || !command.isSetCommandBody()) {
            return null;
        }
        var body = command.getCommandBody();
        if (body.isSetTemplate()) {
            return body.getTemplate().getOperationType();
        } else if (body.isSetGroup()) {
            return body.getGroup().getOperationType();
        } else if (body.isSetReference()) {
            return body.getReference().getOperationType();
        } else if (body.isSetGroupReference()) {
            return body.getGroupReference().getOperationType();
        }
        return null;
    }
}
