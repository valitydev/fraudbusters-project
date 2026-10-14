package dev.vality.fraudbusters.management.service.iface;

import dev.vality.damsel.fraudbusters_notificator.*;

public interface NotificationService {

    Notification create(Notification notification);

    void delete(Long id);

    void updateStatus(Long id, NotificationStatus status);

    ValidationResponse validate(Notification notification);

    NotificationListResponse getAll(Page page, Filter filter);

    Notification getById(Long id);

}
