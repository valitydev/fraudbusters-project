package dev.vality.fraudbusters.notificator.processor;

import dev.vality.fraudbusters.notificator.dao.ChannelDao;
import dev.vality.fraudbusters.notificator.dao.NotificationDao;
import dev.vality.fraudbusters.notificator.dao.NotificationTemplateDao;
import dev.vality.fraudbusters.notificator.dao.ReportNotificationDao;
import dev.vality.fraudbusters.notificator.dao.domain.enums.NotificationStatus;
import dev.vality.fraudbusters.notificator.dao.domain.enums.ReportStatus;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Channel;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Notification;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.NotificationTemplate;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Report;
import dev.vality.fraudbusters.notificator.domain.ReportModel;
import dev.vality.fraudbusters.notificator.serializer.QueryResultSerde;
import dev.vality.fraudbusters.notificator.service.QueryService;
import dev.vality.fraudbusters.notificator.service.iface.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProcessorImpl implements NotificationProcessor {

    private final NotificationDao notificationDao;
    private final NotificationTemplateDao notificationTemplateDao;
    private final ReportNotificationDao reportNotificationDao;
    private final QueryService queryService;
    private final QueryResultSerde queryResultSerde;
    private final NotificationService notificationService;
    private final Predicate<ReportModel> readyForNotifyFilter;
    private final ChannelDao channelDao;

    @Override
    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}")
    @SchedulerLock(name = "TaskScheduler_invoke_process")
    public void process() {
        log.info("NotificationProcessorImpl start process");
        List<Notification> activeNotifications = notificationDao.getByStatus(NotificationStatus.ACTIVE);
        log.info("NotificationProcessorImpl active notifications: {}", activeNotifications);
        if (!CollectionUtils.isEmpty(activeNotifications)) {
            activeNotifications.stream()
                    .map(this::initReportModel)
                    .filter(readyForNotifyFilter)
                    .map(this::enrichByCurrentReport)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(notificationService::send);
        }
        log.info("NotificationProcessorImpl finished process");
    }

    private ReportModel initReportModel(Notification notification) {
        Report report = new Report();
        report.setCreatedAt(LocalDateTime.now());
        report.setNotificationId(notification.getId());
        report.setStatus(ReportStatus.created);

        Channel channel = channelDao.getByName(notification.getChannel());

        return ReportModel.builder()
                .notification(notification)
                .notificationTemplate(notificationTemplateDao.getById(notification.getTemplateId()))
                .currentReport(report)
                .channel(channel)
                .build();
    }

    private Optional<ReportModel> enrichByCurrentReport(final ReportModel reportModel) {
        Notification notification = reportModel.getNotification();
        NotificationTemplate notificationTemplate = reportModel.getNotificationTemplate();
        try {
            List<Map<String, String>> queryResult = queryService.query(notificationTemplate.getQueryText());
            Report currentReport = new Report();
            currentReport.setNotificationId(notification.getId());
            currentReport.setResult(queryResultSerde.serialize(queryResult));
            currentReport.setCreatedAt(LocalDateTime.now());
            currentReport.setStatus(ReportStatus.created);
            Optional<Long> currentReportId = reportNotificationDao.insert(currentReport);
            currentReportId.ifPresent(currentReport::setId);
            log.info("NotificationProcessorImpl enrichByCurrentReport notification: {} template: {} result: {}",
                    notification,
                    notificationTemplate, queryResult);
            reportModel.setCurrentReport(currentReport);
            return Optional.of(reportModel);
        } catch (Exception e) {
            log.error(
                    "NotificationProcessorImpl error when enrichByCurrentReport for notification: {} template: {} e: ",
                    notification, notificationTemplate, e);
        }
        return Optional.empty();
    }

}
