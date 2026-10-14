package dev.vality.fraudbusters.notificator.service;

import dev.vality.fraudbusters.notificator.dao.ReportNotificationDao;
import dev.vality.fraudbusters.notificator.dao.domain.enums.ReportStatus;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Channel;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Report;
import dev.vality.fraudbusters.notificator.domain.ReportModel;
import dev.vality.fraudbusters.notificator.service.factory.MailFactory;
import dev.vality.fraudbusters.notificator.service.factory.TelegramFactory;
import dev.vality.fraudbusters.notificator.service.filter.ChangeQueryResultFilter;
import dev.vality.fraudbusters.notificator.service.iface.MailSenderService;
import dev.vality.fraudbusters.notificator.service.iface.NotificationService;
import dev.vality.fraudbusters.notificator.service.iface.TelegramSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final ReportNotificationDao reportNotificationDao;
    private final MailSenderService mailSenderService;
    private final TelegramSenderService telegramSenderService;
    private final MailFactory mailFactory;
    private final TelegramFactory telegramFactory;
    private final ChangeQueryResultFilter changeQueryResultFilter;

    @Override
    public void send(ReportModel reportModel) {
        log.info("NotificationServiceImpl start send notification");
        Report report = reportModel.getCurrentReport();
        if (!changeQueryResultFilter.test(reportModel)) {
            report.setStatus(ReportStatus.skipped);
            log.info("NotificationServiceImpl skipped: {}", report);
            return;
        }
        Channel channel = reportModel.getChannel();
        if (channel != null && channel.getType() != null) {
            switch (channel.getType()) {
                case mail:
                    sendMail(reportModel, report);
                    break;
                case telegram:
                    sendTelegram(reportModel, report);
                    break;
                default:
                    log.error("Unsupported channel type: {}", channel.getType());
                    report.setStatus(ReportStatus.failed);
            }
        } else {
            sendMail(reportModel, report);
        }

        reportNotificationDao.insert(report);
        log.info("NotificationServiceImpl send: {}", report);
    }

    private void sendMail(ReportModel reportModel, Report report) {
        mailFactory.create(reportModel).ifPresentOrElse(message -> {
            try {
                mailSenderService.send(message);
                report.setStatus(ReportStatus.send);
            } catch (Exception e) {
                log.error("Error when send email message report: {} e: ", report, e);
                report.setStatus(ReportStatus.failed);
            }
        }, () -> report.setStatus(ReportStatus.failed));
    }

    private void sendTelegram(ReportModel reportModel, Report report) {
        telegramFactory.create(reportModel).ifPresentOrElse(message -> {
            try {
                telegramSenderService.send(message);
                report.setStatus(ReportStatus.send);
            } catch (Exception e) {
                log.error("Error when send telegram message report: {} e: ", report, e);
                report.setStatus(ReportStatus.failed);
            }
        }, () -> report.setStatus(ReportStatus.failed));
    }
}
