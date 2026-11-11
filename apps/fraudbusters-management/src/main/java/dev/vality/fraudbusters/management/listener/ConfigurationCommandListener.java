package dev.vality.fraudbusters.management.listener;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.management.service.iface.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/** Projects rule configuration commands of the operation type whose topic the subclass listens to. */
@Slf4j
@RequiredArgsConstructor
public abstract class ConfigurationCommandListener {

    private final InspectStorage storage;
    private final AuditService auditService;

    protected void applyCommandToProjection(Command command,
                                            Function<InspectStorage, ModelProjection<Command, ?>> projectionSelector) {
        ModelProjection<Command, ?> projection = projectionSelector.apply(storage);
        switch (command.getCommandType()) {
            case CREATE -> projection.convertAndCreate(command);
            case DELETE -> projection.convertAndDelete(command);
            default -> log.warn("CommandType not found! command: {}", command);
        }
        auditService.logCommand(command);
    }
}
