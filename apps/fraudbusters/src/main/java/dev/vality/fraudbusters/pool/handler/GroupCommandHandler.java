package dev.vality.fraudbusters.pool.handler;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.listener.AbstractGroupCommandListenerExecutor;
import lombok.RequiredArgsConstructor;

/**
 * Applies group commands to the group pool of one operation type.
 */
@RequiredArgsConstructor
public class GroupCommandHandler extends AbstractGroupCommandListenerExecutor {

    private final InspectStorage storage;

    public void handle(Command command) {
        if (command != null && command.isSetCommandBody() && command.getCommandBody().isSetGroup()) {
            execCommand(command, storage.getGroupPool());
        }
    }

}
