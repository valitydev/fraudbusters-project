package dev.vality.fraudbusters.listener;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.Group;
import dev.vality.damsel.fraudbusters.PriorityId;
import dev.vality.fraudbusters.pool.Pool;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AbstractGroupCommandListenerExecutor {

    protected void execCommand(Command command, Pool<List<String>> pool) {
        Group group = command.getCommandBody().getGroup();
        switch (command.command_type) {
            case CREATE -> createGroup(group, pool);
            case DELETE -> pool.remove(group.getGroupId());
            default -> log.error("Unknown command: {}", command);
        }
    }

    private void createGroup(Group group, Pool<List<String>> pool) {
        List<String> sortedListTemplates = group.template_ids.stream()
                .sorted(Comparator.comparing(PriorityId::getPriority))
                .map(PriorityId::getId)
                // A repeated template is kept at its highest priority: applying it again cannot change the result.
                .distinct()
                .collect(Collectors.toList());
        pool.add(group.getGroupId(), sortedListTemplates);
    }

}
