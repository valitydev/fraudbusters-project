package dev.vality.fraudbusters.management.converter;

import tools.jackson.databind.json.JsonMapper;
import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.management.domain.enums.CommandType;
import dev.vality.fraudbusters.management.domain.enums.ObjectType;
import dev.vality.fraudbusters.management.domain.tables.pojos.CommandAudit;
import dev.vality.geck.common.util.TBaseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandToAuditLogConverter implements Converter<Command, CommandAudit> {

    public static final String UNKNOWN = "UNKNOWN";
    @Autowired
    private JsonMapper objectMapper;

    @Override
    public CommandAudit convert(Command command) {
        CommandAudit model = new CommandAudit();
        model.setCommandType(CommandType.valueOf(command.getCommandType().name()));
        model.setObjectType(TBaseUtil.unionFieldToEnum(command.getCommandBody(), ObjectType.class));
        model.setObject(command.getCommandBody().getFieldValue().toString());
        model.setInitiator(command.getUserInfo() != null && StringUtils.hasLength(command.getUserInfo().getUserId())
                ? command.getUserInfo().getUserId()
                : UNKNOWN);
        return model;
    }
}
