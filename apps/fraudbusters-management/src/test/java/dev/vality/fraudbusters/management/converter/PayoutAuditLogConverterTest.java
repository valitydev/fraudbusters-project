package dev.vality.fraudbusters.management.converter;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandBody;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.GroupReference;
import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.damsel.fraudbusters.Template;
import dev.vality.damsel.wb_list.Event;
import dev.vality.damsel.wb_list.EventType;
import dev.vality.damsel.wb_list.IdInfo;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.PaymentId;
import dev.vality.damsel.wb_list.PayoutId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.domain.enums.ObjectType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayoutAuditLogConverterTest {

    private final CommandToAuditLogConverter commandToAuditLogConverter = new CommandToAuditLogConverter();
    private final EventToAuditLogConverter eventToAuditLogConverter = new EventToAuditLogConverter();

    @Test
    void payoutCommandsAreAuditedApartFromPaymentOnes() {
        assertEquals(ObjectType.template, convertCommand(CommandBody.template(new Template()
                .setId("template-1")
                .setTemplate(new byte[0])))
                .getObjectType());
        assertEquals(ObjectType.payout_template, convertCommand(CommandBody.template(new Template()
                .setId("template-1")
                .setTemplate(new byte[0])
                .setOperationType(OperationType.payout)))
                .getObjectType());
        assertEquals(ObjectType.payout_group_reference, convertCommand(CommandBody.group_reference(
                new GroupReference()
                        .setGroupId("group-1")
                        .setOperationType(OperationType.payout)))
                .getObjectType());
    }

    @Test
    void payoutListEventsAreAuditedApartFromPaymentOnes() {
        assertEquals(ObjectType.payout_grey, convertEvent(IdInfo.payout_id(new PayoutId()), ListType.grey)
                .getObjectType());
        assertEquals(ObjectType.grey, convertEvent(IdInfo.payment_id(new PaymentId()), ListType.grey)
                .getObjectType());
    }

    private dev.vality.fraudbusters.management.domain.tables.pojos.CommandAudit convertCommand(
            CommandBody commandBody) {
        return commandToAuditLogConverter.convert(new Command()
                .setCommandType(CommandType.CREATE)
                .setCommandBody(commandBody));
    }

    private dev.vality.fraudbusters.management.domain.tables.pojos.CommandAudit convertEvent(IdInfo id,
                                                                                         ListType listType) {
        return eventToAuditLogConverter.convert(new Event()
                .setEventType(EventType.CREATED)
                .setRow(new Row()
                        .setId(id)
                        .setListName("EMAIL")
                        .setListType(listType)));
    }
}
