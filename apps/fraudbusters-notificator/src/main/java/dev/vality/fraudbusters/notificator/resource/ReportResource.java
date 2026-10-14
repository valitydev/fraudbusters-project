package dev.vality.fraudbusters.notificator.resource;

import dev.vality.fraudbusters.notificator.dao.domain.enums.ReportStatus;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Report;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportResource {

    List<Report> findReportsByStatusAndFromTime(ReportStatus status, LocalDateTime from);

}
