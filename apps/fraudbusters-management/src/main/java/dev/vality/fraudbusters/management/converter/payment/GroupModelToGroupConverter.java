package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.fraudbusters.management.domain.GroupModel;
import dev.vality.fraudbusters.management.domain.PriorityIdModel;
import dev.vality.swag.fraudbusters.management.model.Group;
import dev.vality.swag.fraudbusters.management.model.PriorityId;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class GroupModelToGroupConverter
        implements Converter<GroupModel, Group> {

    private final PriorityModelToPriorityIdConverter priorityModelToPriorityIdConverter;

    @NonNull
    @Override
    public Group convert(GroupModel groupModel) {
        return new Group()
                .groupId(groupModel.getGroupId())
                .priorityTemplates(convertPriorityTemplates(groupModel.getPriorityTemplates()))
                .modifiedByUser(groupModel.getModifiedByUser());
    }

    private List<PriorityId> convertPriorityTemplates(List<PriorityIdModel> priorityTemplates) {
        return priorityTemplates.stream()
                .map(priorityModelToPriorityIdConverter::destinationToSource)
                .collect(Collectors.toList());
    }
}
