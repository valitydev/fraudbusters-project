package dev.vality.fraudbusters.management.service.iface;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.wb_list.Event;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.swag.fraudbusters.management.model.FilterLogsResponse;

import java.util.List;

public interface AuditService {

    void logCommand(Command command);

    void logEvent(Event event);

    FilterLogsResponse filterRecords(List<String> commandTypes, List<String> objectTypes, String from,
                                     String to, FilterRequest filterRequest);
}
