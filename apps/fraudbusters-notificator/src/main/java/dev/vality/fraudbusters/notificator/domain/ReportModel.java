package dev.vality.fraudbusters.notificator.domain;

import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Channel;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Notification;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.NotificationTemplate;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Report;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportModel {

    private Notification notification;
    private NotificationTemplate notificationTemplate;
    private Report currentReport;
    private Report previousReport;
    private Channel channel;

}
