package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.management.converter.CommandToGroupModelConverter;
import dev.vality.fraudbusters.management.domain.GroupModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** A payout group keeps who changed it last. */
@Component
@RequiredArgsConstructor
public class CommandToPayoutGroupModelConverter implements Converter<Command, GroupModel> {

    private final CommandToGroupModelConverter commandToGroupModelConverter;

    @Override
    public GroupModel convert(Command command) {
        GroupModel model = commandToGroupModelConverter.convert(command);
        model.setModifiedByUser(command.isSetUserInfo() ? command.getUserInfo().getUserId() : null);
        return model;
    }
}
