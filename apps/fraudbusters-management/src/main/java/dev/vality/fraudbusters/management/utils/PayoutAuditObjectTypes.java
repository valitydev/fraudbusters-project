package dev.vality.fraudbusters.management.utils;

import dev.vality.damsel.fraudbusters.CommandBody;
import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.fraudbusters.management.domain.enums.ObjectType;
import lombok.experimental.UtilityClass;

/** Payout changes are audited under their own object types, so the audit log tells them apart from payments. */
@UtilityClass
public class PayoutAuditObjectTypes {

    /** A command without an operation type keeps its historical meaning: payment. */
    public static boolean isPayoutCommand(CommandBody commandBody) {
        OperationType operationType = switch (commandBody.getSetField()) {
            case TEMPLATE -> commandBody.getTemplate().getOperationType();
            case REFERENCE -> commandBody.getReference().getOperationType();
            case GROUP -> commandBody.getGroup().getOperationType();
            case GROUP_REFERENCE -> commandBody.getGroupReference().getOperationType();
            default -> null;
        };
        return operationType == OperationType.payout;
    }

    public static ObjectType convertToPayoutObjectType(ObjectType objectType) {
        return switch (objectType) {
            case template -> ObjectType.payout_template;
            case reference -> ObjectType.payout_reference;
            case group_reference -> ObjectType.payout_group_reference;
            case group -> ObjectType.payout_group;
            case black -> ObjectType.payout_black;
            case white -> ObjectType.payout_white;
            case grey -> ObjectType.payout_grey;
            case naming -> ObjectType.payout_naming;
            default -> throw new IllegalArgumentException("Payouts have no audit object type for " + objectType);
        };
    }
}
