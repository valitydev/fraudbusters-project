package dev.vality.fraudbusters.management.converter;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.Group;
import dev.vality.damsel.fraudbusters.PriorityId;
import dev.vality.fraudbusters.management.domain.GroupModel;
import dev.vality.fraudbusters.management.domain.PriorityIdModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class CommandToGroupModelConverter implements Converter<Command, GroupModel> {

    @Override
    public GroupModel convert(Command command) {
        GroupModel model = new GroupModel();
        Group group = command.getCommandBody().getGroup();
        model.setGroupId(group.getGroupId());
        List<PriorityId> templateIds = group.getTemplateIds();
        if (!CollectionUtils.isEmpty(templateIds)) {
            model.setPriorityTemplates(convertPriorityIds(templateIds));
        }
        return model;
    }

    private List<PriorityIdModel> convertPriorityIds(List<PriorityId> templateIds) {
        return templateIds.stream()
                .map(priorityId -> new PriorityIdModel(priorityId.getPriority(), priorityId.getId(), null))
                .collect(Collectors.toList());
    }
}
