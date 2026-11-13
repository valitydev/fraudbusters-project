package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.management.converter.CommandToTemplateModelConverter;
import dev.vality.fraudbusters.management.domain.TemplateModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** A payout template keeps who changed it last. */
@Component
@RequiredArgsConstructor
public class CommandToPayoutTemplateModelConverter implements Converter<Command, TemplateModel> {

    private final CommandToTemplateModelConverter commandToTemplateModelConverter;

    @Override
    public TemplateModel convert(Command command) {
        TemplateModel model = commandToTemplateModelConverter.convert(command);
        model.setModifiedByUser(command.isSetUserInfo() ? command.getUserInfo().getUserId() : null);
        return model;
    }
}
