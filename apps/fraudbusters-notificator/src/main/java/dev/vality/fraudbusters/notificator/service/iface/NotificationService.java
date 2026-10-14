package dev.vality.fraudbusters.notificator.service.iface;

import dev.vality.fraudbusters.notificator.domain.ReportModel;

public interface NotificationService {

    void send(ReportModel reportModel);

}
