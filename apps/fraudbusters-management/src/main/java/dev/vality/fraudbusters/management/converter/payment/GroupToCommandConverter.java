package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandBody;
import dev.vality.damsel.fraudbusters.Group;
import dev.vality.damsel.fraudbusters.PriorityId;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class GroupToCommandConverter
        implements Converter<dev.vality.swag.fraudbusters.management.model.Group, Command> {

    @NonNull
    @Override
    public Command convert(dev.vality.swag.fraudbusters.management.model.Group groupModel) {
        return new Command()
                .setCommandBody(CommandBody.group(new Group()
                        .setGroupId(groupModel.getGroupId())
                        .setTemplateIds(groupModel.getPriorityTemplates().stream()
                                .map(pair -> new PriorityId()
                                        .setPriority(pair.getPriority())
                                        .setId(pair.getId()))
                                .collect(Collectors.toList()))));
    }
}
