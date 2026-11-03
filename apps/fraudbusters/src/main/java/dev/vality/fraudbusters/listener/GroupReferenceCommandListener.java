package dev.vality.fraudbusters.listener;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.GroupReference;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.util.OperationTypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Keeps the group references of the operation type whose topic the subclass listens to. */
@Slf4j
@RequiredArgsConstructor
public abstract class GroupReferenceCommandListener extends AbstractPoolCommandListenerExecutor
        implements CommandListener {

    private final InspectStorage storage;

    protected void handle(Command command) {
        log.info("{} command: {}", getClass().getSimpleName(), command);
        if (command != null && command.isSetCommandBody() && command.getCommandBody().isSetGroupReference()) {
            if (OperationTypeUtil.declaresOther(command, storage.getOperationType())) {
                log.warn("{} skips command of another operation type: {}", getClass().getSimpleName(), command);
                return;
            }
            GroupReference reference = command.getCommandBody().getGroupReference();
            String key = storage.groupReferenceKey(reference);
            execCommand(command, key, storage.getGroupReferencePool(), reference::getGroupId);
        }
    }

}
