package dev.vality.fraudbusters.notificator.dao;


import dev.vality.fraudbusters.notificator.dao.domain.enums.NotificationStatus;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Notification;
import dev.vality.fraudbusters.notificator.service.dto.FilterDto;

import java.util.List;

public interface NotificationDao {

    Notification insert(Notification notification);

    void remove(Long id);

    Notification getById(Long id);

    List<Notification> getByStatus(NotificationStatus status);

    List<Notification> getAll(FilterDto filter);

}
