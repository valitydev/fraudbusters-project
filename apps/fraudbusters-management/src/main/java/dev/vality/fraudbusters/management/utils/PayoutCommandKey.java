package dev.vality.fraudbusters.management.utils;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.OperationType;

import java.nio.charset.StandardCharsets;

public final class PayoutCommandKey {

    // PostgreSQL payment identifiers cannot contain NUL, so legacy keys cannot collide with this namespace.
    private static final String PREFIX = "\0payout:";

    private PayoutCommandKey() {
    }

    public static String forCommand(Command command, String paymentKey) {
        var body = command.getCommandBody();
        if (body.isSetTemplate() && body.getTemplate().getOperationType() == OperationType.payout) {
            return PREFIX + "template:" + encode(body.getTemplate().getId());
        }
        if (body.isSetGroup() && body.getGroup().getOperationType() == OperationType.payout) {
            return PREFIX + "group:" + encode(body.getGroup().getGroupId());
        }
        if (body.isSetReference() && body.getReference().getOperationType() == OperationType.payout) {
            var reference = body.getReference();
            if (reference.isIsGlobal()) {
                return PREFIX + "reference:global";
            }
            return PREFIX + "reference:" + encode(reference.getPartyId()) + encode(reference.getWalletId());
        }
        if (body.isSetGroupReference() && body.getGroupReference().getOperationType() == OperationType.payout) {
            var reference = body.getGroupReference();
            return PREFIX + "group-reference:" + encode(reference.getPartyId()) + encode(reference.getWalletId());
        }
        return paymentKey;
    }

    private static String encode(String value) {
        return value == null || value.isEmpty() ? "0:" : value.getBytes(StandardCharsets.UTF_8).length + ":" + value;
    }
}
