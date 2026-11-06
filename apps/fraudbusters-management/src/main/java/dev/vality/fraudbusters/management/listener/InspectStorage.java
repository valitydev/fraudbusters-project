package dev.vality.fraudbusters.management.listener;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.damsel.wb_list.Event;

/** Tables that keep the rule configuration and lists of one operation type. */
public record InspectStorage(OperationType operationType,
                             ModelProjection<Command, ?> templates,
                             ModelProjection<Command, ?> groups,
                             ModelProjection<Command, ?> references,
                             ModelProjection<Command, ?> groupReferences,
                             ModelProjection<Event, ?> lists) {
}
