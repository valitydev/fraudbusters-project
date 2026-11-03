package dev.vality.fraudbusters.management.listener;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.management.service.iface.AuditService;
import dev.vality.fraudbusters.management.utils.OperationTypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/** Projects rule configuration commands of the operation type whose topic the subclass listens to. */
@Slf4j
@RequiredArgsConstructor
public abstract class ConfigurationCommandListener extends CommandListener {

    private final InspectStorage storage;
    private final AuditService auditService;

    protected void apply(Command command, Function<InspectStorage, ModelProjection<Command, ?>> projection) {
        if (OperationTypeUtil.declaresOther(command, storage.operationType())) {
            log.warn("{} skips command of another operation type: {}", getClass().getSimpleName(), command);
            return;
        }
        handle(command, projection.apply(storage));
        auditService.logCommand(command);
    }
}
