package dev.vality.fraudbusters.management.service;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.wb_list.Event;
import dev.vality.fraudbusters.management.converter.CommandToAuditLogConverter;
import dev.vality.fraudbusters.management.converter.CommonAuditInternalToCommonAuditConverter;
import dev.vality.fraudbusters.management.converter.EventToAuditLogConverter;
import dev.vality.fraudbusters.management.dao.audit.CommandAuditDao;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.tables.pojos.CommandAudit;
import dev.vality.fraudbusters.management.service.iface.AuditService;
import dev.vality.fraudbusters.management.utils.DateTimeUtils;
import dev.vality.swag.fraudbusters.management.model.FilterLogsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final CommandAuditDao commandAuditDao;
    private final CommandToAuditLogConverter commandToAuditLogConverter;
    private final EventToAuditLogConverter eventToAuditLogConverter;
    private final CommonAuditInternalToCommonAuditConverter commonAuditInternalToCommonAuditConverter;

    @Override
    public FilterLogsResponse filterRecords(List<String> commandTypes, List<String> objectTypes, String from,
                                            String to, FilterRequest filterRequest) {
        var fromDate = LocalDateTime.parse(from, DateTimeUtils.DATE_TIME_FORMATTER);
        var toDate = LocalDateTime.parse(to, DateTimeUtils.DATE_TIME_FORMATTER);
        List<CommandAudit> commandAudits = commandAuditDao.filterLog(fromDate, toDate, commandTypes,
                objectTypes, filterRequest);
        Integer count = commandAuditDao.countFilterRecords(fromDate, toDate, commandTypes,
                objectTypes, filterRequest);
        return new FilterLogsResponse()
                .count(count)
                .result(commonAuditInternalToCommonAuditConverter.convert(commandAudits));
    }

    @Override
    public void logCommand(Command command) {
        CommandAudit commandAudit = commandToAuditLogConverter.convert(command);
        commandAuditDao.insert(commandAudit);
    }

    @Override
    public void logEvent(Event command) {
        CommandAudit commandAudit = eventToAuditLogConverter.convert(command);
        commandAuditDao.insert(commandAudit);
    }

}
