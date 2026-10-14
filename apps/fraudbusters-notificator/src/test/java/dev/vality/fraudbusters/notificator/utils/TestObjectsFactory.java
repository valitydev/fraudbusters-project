package dev.vality.fraudbusters.notificator.utils;

import dev.vality.damsel.fraudbusters_notificator.Channel;
import dev.vality.damsel.fraudbusters_notificator.Notification;
import dev.vality.fraudbusters.notificator.constant.TemplateType;
import dev.vality.fraudbusters.notificator.dao.domain.enums.ChannelType;
import dev.vality.fraudbusters.notificator.dao.domain.enums.NotificationStatus;
import dev.vality.fraudbusters.notificator.dao.domain.enums.ReportStatus;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.NotificationTemplate;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Report;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.ChannelRecord;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.NotificationRecord;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.NotificationTemplateRecord;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.ReportRecord;
import dev.vality.fraudbusters.warehouse.Row;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public abstract class TestObjectsFactory {

    public static Row testRow() {
        Map<String, String> rowValues = new HashMap<>();
        rowValues.put(randomString(), randomString());
        rowValues.put(randomString(), randomString());
        rowValues.put(randomString(), randomString());
        Row row = new Row();
        row.setValues(rowValues);
        return row;
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    public static Notification testNotification() {
        Notification notification = new Notification();
        notification.setFrequency("1s");
        LocalDateTime now = LocalDateTime.now();
        notification.setName(randomString());
        notification.setCreatedAt(now.toString());
        notification.setUpdatedAt(now.toString());
        notification.setChannel(randomString());
        notification.setPeriod("1d");
        notification.setStatus(dev.vality.damsel.fraudbusters_notificator.NotificationStatus.ACTIVE);
        notification.setSubject(randomString());
        return notification;
    }

    public static dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Notification testTableNotification() {
        var notification = new dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Notification();
        notification.setFrequency("1s");
        LocalDateTime now = LocalDateTime.now();
        notification.setName(randomString());
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);
        notification.setChannel(randomString());
        notification.setPeriod("1d");
        notification.setStatus(NotificationStatus.ACTIVE);
        notification.setSubject(randomString());
        return notification;
    }

    public static NotificationRecord testNotificationRecord() {
        NotificationRecord notification = new NotificationRecord();
        notification.setFrequency("1s");
        LocalDateTime now = LocalDateTime.now();
        notification.setName(randomString());
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);
        notification.setChannel(randomString());
        notification.setPeriod("1d");
        notification.setStatus(NotificationStatus.ACTIVE);
        notification.setSubject(randomString());
        return notification;
    }

    public static NotificationTemplate testNotificationTemplate(String groupParams) {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        LocalDateTime now = LocalDateTime.now();
        notificationTemplate.setName(randomString());
        notificationTemplate.setCreatedAt(now);
        notificationTemplate.setUpdatedAt(now);
        notificationTemplate.setBasicParams(groupParams);
        notificationTemplate.setQueryText(randomString());
        notificationTemplate.setType(TemplateType.MAIL_FORM.name());
        notificationTemplate.setSkeleton(randomString());
        return notificationTemplate;
    }

    public static NotificationTemplateRecord testNotificationTemplateRecord() {
        NotificationTemplateRecord notificationTemplateRecord = new NotificationTemplateRecord();
        LocalDateTime now = LocalDateTime.now();
        notificationTemplateRecord.setName(randomString());
        notificationTemplateRecord.setCreatedAt(now);
        notificationTemplateRecord.setUpdatedAt(now);
        notificationTemplateRecord.setBasicParams(randomString());
        notificationTemplateRecord.setQueryText(randomString());
        notificationTemplateRecord.setType(TemplateType.MAIL_FORM.name());
        notificationTemplateRecord.setSkeleton(randomString());
        return notificationTemplateRecord;
    }

    public static ChannelRecord testChannelRecord() {
        ChannelRecord channelRecord = new ChannelRecord();
        channelRecord.setName(randomString());
        channelRecord.setDestination(" test@mail.ru, two@test.ru");
        channelRecord.setType(ChannelType.mail);
        return channelRecord;
    }

    public static Channel testChannel() {
        Channel channel = new Channel();
        channel.setName(randomString());
        channel.setDestination(" test@mail.ru, two@test.ru");
        channel.setType(dev.vality.damsel.fraudbusters_notificator.ChannelType.mail);
        return channel;
    }

    public static ReportRecord testReportRecord() {
        ReportRecord report = new ReportRecord();
        report.setNotificationId(new Random().nextLong());
        report.setStatus(ReportStatus.send);
        report.setResult(randomString());
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    public static @NotNull Report createReport() {
        Report currentReport = new Report();
        currentReport.setStatus(ReportStatus.created);
        currentReport.setCreatedAt(LocalDateTime.now());
        currentReport.setResult("test-result");
        return currentReport;
    }

    public static @NotNull dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Notification
                                    createNotification() {
        dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Notification
                notification = new dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Notification();
        notification.setName("test-notification");
        notification.setStatus(NotificationStatus.ACTIVE);
        notification.setId(1L);
        notification.setChannel("channel");
        return notification;
    }

    public static @NotNull dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Channel createChannel() {
        return new dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Channel("test", LocalDateTime.now(),
                ChannelType.telegram, "test");
    }

}
