package dev.vality.fraudbusters.management.utils;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.OperationType;

public final class OperationTypeUtil {

    private OperationTypeUtil() {
    }

    /** The topic defines the operation type; a command may only confirm it, not declare another one. */
    public static boolean declaresOther(Command command, OperationType topicType) {
        OperationType declared = declared(command);
        return declared != null && declared != topicType;
    }

    private static OperationType declared(Command command) {
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
