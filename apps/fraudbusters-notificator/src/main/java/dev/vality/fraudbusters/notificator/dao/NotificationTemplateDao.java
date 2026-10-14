package dev.vality.fraudbusters.notificator.dao;


import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.NotificationTemplate;

import java.util.List;

public interface NotificationTemplateDao {

    NotificationTemplate getById(Integer id);

    List<NotificationTemplate> getAll();


}
