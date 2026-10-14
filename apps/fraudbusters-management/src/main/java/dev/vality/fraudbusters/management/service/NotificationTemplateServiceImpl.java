package dev.vality.fraudbusters.management.service;

import dev.vality.damsel.fraudbusters_notificator.NotificationTemplateListResponse;
import dev.vality.damsel.fraudbusters_notificator.NotificationTemplateServiceSrv;
import dev.vality.fraudbusters.management.exception.NotificatorCallException;
import dev.vality.fraudbusters.management.service.iface.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateServiceSrv.Iface notificationTemplateClient;

    @Override
    public NotificationTemplateListResponse getAll() {
        try {
            return notificationTemplateClient.getAll();
        } catch (TException e) {
            log.error("Error call notificator getAll templates ", e);
            throw new NotificatorCallException("Error call notificator getAll templates");
        }
    }
}
