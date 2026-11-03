package dev.vality.fraudbusters.listener;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.TemplateReference;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.util.OperationTypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Keeps the template references of the operation type whose topic the subclass listens to. */
@Slf4j
@RequiredArgsConstructor
public abstract class TemplateReferenceCommandListener extends AbstractPoolCommandListenerExecutor
        implements CommandListener {

    private final InspectStorage storage;

    protected void handle(Command command) {
        log.info("{} command: {}", getClass().getSimpleName(), command);
        if (command != null && command.isSetCommandBody() && command.getCommandBody().isSetReference()) {
            if (OperationTypeUtil.declaresOther(command, storage.getOperationType())) {
                log.warn("{} skips command of another operation type: {}", getClass().getSimpleName(), command);
                return;
            }
            TemplateReference reference = command.getCommandBody().getReference();
            String key = storage.referenceKey(reference);
            execCommand(command, key, storage.getReferencePool(), reference::getTemplateId);
        }
    }

}
