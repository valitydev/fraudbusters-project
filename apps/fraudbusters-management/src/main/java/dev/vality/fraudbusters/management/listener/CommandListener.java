package dev.vality.fraudbusters.management.listener;

import dev.vality.damsel.fraudbusters.Command;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class CommandListener {

    protected void handle(Command command, ModelProjection<Command, ?> projection) {
        switch (command.getCommandType()) {
            case CREATE -> projection.create(command);
            case DELETE -> projection.delete(command);
            default -> log.warn("CommandType not found! command: {}", command);
        }
    }
}
