package dev.vality.fraudbusters.notificator.resource;

import dev.vality.fraudbusters.notificator.dao.ReportNotificationDao;
import dev.vality.fraudbusters.notificator.dao.domain.enums.ReportStatus;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Report;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReportResourceImpl implements ReportResource { // TODO забыли про этот метод, он нужен?

    private final ReportNotificationDao reportNotificationDao;

    @Override
    @GetMapping(value = "/reports")
    public List<Report> findReportsByStatusAndFromTime(@Validated @RequestParam("status") ReportStatus status,
                                                       @Validated @RequestParam("from")
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                       LocalDateTime from) {
        List<Report> reports = reportNotificationDao.getReportsByStatusAndFromTime(status, from);
        log.info("ReportResourceImpl findReportsByStatusAndFromTime reports: {}", reports);
        return reports;
    }

}
