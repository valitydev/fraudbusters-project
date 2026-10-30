package dev.vality.fraudbusters.util;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.OperationType;

public final class OperationTypeUtil {

    private OperationTypeUtil() {
    }

    public static boolean isPayout(Command command) {
        if (command == null || !command.isSetCommandBody()) {
            return false;
        }
        var body = command.getCommandBody();
        OperationType type = null;
        if (body.isSetTemplate()) {
            type = body.getTemplate().getOperationType();
        } else if (body.isSetGroup()) {
            type = body.getGroup().getOperationType();
        } else if (body.isSetReference()) {
            type = body.getReference().getOperationType();
            if (type != OperationType.payout && body.getReference().isSetWalletId()) {
                throw new IllegalArgumentException("wallet_id is not supported for payments");
            }
        } else if (body.isSetGroupReference()) {
            type = body.getGroupReference().getOperationType();
            if (type != OperationType.payout && body.getGroupReference().isSetWalletId()) {
                throw new IllegalArgumentException("wallet_id is not supported for payments");
            }
        }
        return type == OperationType.payout;
    }
}
