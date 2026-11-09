package dev.vality.fraudbusters.pool.handler;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.GroupReference;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.listener.AbstractPoolCommandListenerExecutor;
import lombok.RequiredArgsConstructor;

/**
 * Applies group reference commands to the group reference pool of one operation type.
 */
@RequiredArgsConstructor
public class GroupReferenceCommandHandler extends AbstractPoolCommandListenerExecutor {

    private final InspectStorage storage;

    public void handle(Command command) {
        if (command != null && command.isSetCommandBody() && command.getCommandBody().isSetGroupReference()) {
            GroupReference reference = command.getCommandBody().getGroupReference();
            String key = storage.generateGroupReferenceKey(reference);
            execCommand(command, key, storage.getGroupReferencePool(), reference::getGroupId);
        }
    }

}
