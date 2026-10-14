package dev.vality.fraudbusters.management.converter;

import dev.vality.damsel.wb_list.*;
import dev.vality.fraudbusters.management.domain.tables.pojos.CommandAudit;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EventToAuditLogConverterTest {

    private final EventToAuditLogConverter eventToAuditLogConverter = new EventToAuditLogConverter();

    @Test
    void convert() {
        CommandAudit audit = eventToAuditLogConverter.convert(new Event().setEventType(EventType.CREATED)
                .setRow(new Row()
                        .setId(IdInfo.p2p_id(new P2pId()))
                        .setListName("list")
                        .setListType(ListType.black)
                )
                .setUserInfo(new UserInfo())
                .setCommandTime(""));

        assertTrue(StringUtils.hasLength(audit.getObject()));

        audit = eventToAuditLogConverter.convert(new Event().setEventType(EventType.CREATED)
                .setRow(new Row()
                        .setId(IdInfo.payment_id(new PaymentId()))
                        .setListName("list")
                        .setListType(ListType.black)
                )
                .setUserInfo(new UserInfo())
                .setCommandTime(""));

        assertTrue(StringUtils.hasLength(audit.getObject()));
    }
}
