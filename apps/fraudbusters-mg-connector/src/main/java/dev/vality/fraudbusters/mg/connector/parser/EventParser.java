package dev.vality.fraudbusters.mg.connector.parser;

import dev.vality.machinegun.eventsink.MachineEvent;

public interface EventParser<T> {

    T parseEvent(MachineEvent message);

}
