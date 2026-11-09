package dev.vality.fraudbusters.pool.handler;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.TemplateReference;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.listener.AbstractPoolCommandListenerExecutor;
import lombok.RequiredArgsConstructor;

/**
 * Applies template reference commands to the reference pool of one operation type.
 */
@RequiredArgsConstructor
public class TemplateReferenceCommandHandler extends AbstractPoolCommandListenerExecutor {

    private final InspectStorage storage;

    public void handle(Command command) {
        if (command != null && command.isSetCommandBody() && command.getCommandBody().isSetReference()) {
            TemplateReference reference = command.getCommandBody().getReference();
            String key = storage.generateReferenceKey(reference);
            execCommand(command, key, storage.getReferencePool(), reference::getTemplateId);
        }
    }

}
