package dev.vality.fraudbusters.management.listener;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.wb_list.Event;
import dev.vality.damsel.wb_list.Row;

import java.util.function.Predicate;

/** Tables that keep the rule configuration and lists of one operation type. */
public record InspectStorage(Predicate<Row> rowMatcher,
                             ModelProjection<Command, ?> templates,
                             ModelProjection<Command, ?> groups,
                             ModelProjection<Command, ?> references,
                             ModelProjection<Command, ?> groupReferences,
                             ModelProjection<Event, ?> lists) {

    public boolean canHandle(Event event) {
        return rowMatcher.test(event.getRow());
    }
}
