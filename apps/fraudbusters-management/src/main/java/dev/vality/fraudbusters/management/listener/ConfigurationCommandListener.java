package dev.vality.fraudbusters.management.listener;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.management.service.iface.AuditService;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

/** Projects rule configuration commands of the operation type whose topic the subclass listens to. */
@RequiredArgsConstructor
public abstract class ConfigurationCommandListener extends CommandListener {

    private final InspectStorage storage;
    private final AuditService auditService;

    protected void apply(Command command, Function<InspectStorage, ModelProjection<Command, ?>> projection) {
        handle(command, projection.apply(storage));
        auditService.logCommand(command);
    }
}
