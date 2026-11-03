package dev.vality.fraudbusters.management.utils;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.OperationType;

public final class OperationTypeUtil {

    private OperationTypeUtil() {
    }

    /** A missing operation type keeps its historical meaning: payment. */
    public static OperationType resolve(Command command) {
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
        return type == null ? OperationType.payment : type;
    }
}
