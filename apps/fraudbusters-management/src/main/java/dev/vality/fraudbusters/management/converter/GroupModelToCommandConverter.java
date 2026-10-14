package dev.vality.fraudbusters.management.converter;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandBody;
import dev.vality.damsel.fraudbusters.Group;
import dev.vality.damsel.fraudbusters.PriorityId;
import dev.vality.fraudbusters.management.domain.GroupModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class GroupModelToCommandConverter implements Converter<GroupModel, Command> {

    @NonNull
    @Override
    public Command convert(GroupModel groupModel) {
        var command = new Command();
        var group = new Group();
        group.setGroupId(groupModel.getGroupId());
        group.setTemplateIds(groupModel.getPriorityTemplates().stream()
                .map(pair -> new PriorityId()
                        .setPriority(pair.getPriority())
                        .setId(pair.getId()))
                .collect(Collectors.toList()));
        command.setCommandBody(CommandBody.group(group));
        return command;
    }
}
