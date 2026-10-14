package dev.vality.fraudbusters.management.dao.audit;

import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.tables.pojos.CommandAudit;

import java.time.LocalDateTime;
import java.util.List;

public interface CommandAuditDao {

    void insert(CommandAudit log);

    List<CommandAudit> filterLog(LocalDateTime from,
                                 LocalDateTime to,
                                 List<String> commandTypes,
                                 List<String> objectTypes,
                                 FilterRequest filterRequest);

    Integer countFilterRecords(LocalDateTime from,
                               LocalDateTime to,
                               List<String> commandTypes,
                               List<String> objectTypes,
                               FilterRequest filterRequest);

}
