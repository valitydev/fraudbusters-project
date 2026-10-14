package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandBody;
import dev.vality.damsel.fraudbusters.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class TemplateToCommandConverter
        implements Converter<dev.vality.swag.fraudbusters.management.model.Template, Command> {

    @Override
    @NonNull
    public Command convert(dev.vality.swag.fraudbusters.management.model.Template templateModel) {
        var command = new Command();
        var template = new Template();
        template.setId(templateModel.getId());
        template.setTemplate(templateModel.getTemplate().getBytes());
        command.setCommandBody(CommandBody.template(template));
        return command;
    }
}
